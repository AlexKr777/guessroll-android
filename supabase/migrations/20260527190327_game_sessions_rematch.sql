-- GuessRoll game sessions and real rematch support.
--
-- Safe scope:
-- - creates a session boundary for each game run;
-- - keeps existing rooms, players, media_items, rounds, and guesses;
-- - backfills legacy rounds/guesses into one session per room;
-- - changes round-number uniqueness from room-scoped to session-scoped;
-- - adds game_sessions to Supabase Realtime publication.
--
-- This migration does not drop tables, truncate data, or delete rows.

create table if not exists public.game_sessions (
    id uuid primary key default gen_random_uuid(),
    room_id uuid not null references public.rooms(id) on delete cascade,
    status text not null default 'playing' check (status in ('playing', 'finished')),
    round_count int not null check (round_count between 1 and 50),
    current_round_index int not null default 0,
    started_at timestamptz not null default now(),
    ended_at timestamptz null,
    created_at timestamptz not null default now()
);

alter table public.rooms
    add column if not exists current_game_session_id uuid null;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'rooms_current_game_session_id_fkey'
          and conrelid = 'public.rooms'::regclass
    ) then
        alter table public.rooms
            add constraint rooms_current_game_session_id_fkey
            foreign key (current_game_session_id)
            references public.game_sessions(id)
            on delete set null;
    end if;
end $$;

alter table public.rounds
    add column if not exists game_session_id uuid null;

alter table public.guesses
    add column if not exists game_session_id uuid null;

create index if not exists game_sessions_room_id_idx on public.game_sessions (room_id);
create index if not exists game_sessions_room_status_idx on public.game_sessions (room_id, status);
create index if not exists rounds_game_session_id_idx on public.rounds (game_session_id);
create index if not exists guesses_game_session_id_idx on public.guesses (game_session_id);

-- Legacy backfill: rooms that already have rounds get one session, so old
-- results remain readable while future rematches create additional sessions.
insert into public.game_sessions (
    room_id,
    status,
    round_count,
    current_round_index,
    started_at,
    ended_at,
    created_at
)
select
    r.id,
    case when r.status = 'finished' then 'finished' else 'playing' end,
    greatest(count(rd.id)::int, 1),
    r.current_round_index,
    r.created_at,
    case when r.status = 'finished' then now() else null end,
    r.created_at
from public.rooms r
join public.rounds rd on rd.room_id = r.id
where not exists (
    select 1
    from public.game_sessions gs
    where gs.room_id = r.id
)
group by r.id, r.status, r.current_round_index, r.created_at;

update public.rooms r
set current_game_session_id = (
    select gs_inner.id
    from public.game_sessions gs_inner
    where gs_inner.room_id = r.id
    order by gs_inner.created_at desc, gs_inner.id desc
    limit 1
)
where r.current_game_session_id is null
  and exists (
      select 1
      from public.rounds rd
      where rd.room_id = r.id
  );

update public.rounds rd
set game_session_id = r.current_game_session_id
from public.rooms r
where rd.room_id = r.id
  and rd.game_session_id is null
  and r.current_game_session_id is not null;

update public.guesses g
set game_session_id = rd.game_session_id
from public.rounds rd
where g.round_id = rd.id
  and g.game_session_id is null
  and rd.game_session_id is not null;

do $$
begin
    if exists (
        select 1
        from public.rounds
        where game_session_id is null
    ) then
        raise exception 'Cannot make rounds.game_session_id required: unscoped legacy rounds remain';
    end if;

    if exists (
        select 1
        from public.guesses
        where game_session_id is null
    ) then
        raise exception 'Cannot make guesses.game_session_id required: unscoped legacy guesses remain';
    end if;
end $$;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'rounds_game_session_id_fkey'
          and conrelid = 'public.rounds'::regclass
    ) then
        alter table public.rounds
            add constraint rounds_game_session_id_fkey
            foreign key (game_session_id)
            references public.game_sessions(id)
            on delete cascade;
    end if;

    if not exists (
        select 1
        from pg_constraint
        where conname = 'guesses_game_session_id_fkey'
          and conrelid = 'public.guesses'::regclass
    ) then
        alter table public.guesses
            add constraint guesses_game_session_id_fkey
            foreign key (game_session_id)
            references public.game_sessions(id)
            on delete cascade;
    end if;
end $$;

alter table public.rounds
    alter column game_session_id set not null;

alter table public.guesses
    alter column game_session_id set not null;

alter table public.rounds
    drop constraint if exists rounds_room_id_round_number_key;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'rounds_game_session_id_round_number_key'
          and conrelid = 'public.rounds'::regclass
    ) then
        alter table public.rounds
            add constraint rounds_game_session_id_round_number_key
            unique (game_session_id, round_number);
    end if;
end $$;

alter table public.game_sessions enable row level security;

grant select, insert, update on public.game_sessions to authenticated;

drop policy if exists "game_sessions_select_authenticated" on public.game_sessions;
create policy "game_sessions_select_authenticated"
on public.game_sessions for select
to authenticated
using (true);

drop policy if exists "game_sessions_insert_host" on public.game_sessions;
create policy "game_sessions_insert_host"
on public.game_sessions for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = game_sessions.room_id
          and p.auth_user_id = auth.uid()
          and p.is_host = true
    )
);

drop policy if exists "game_sessions_update_host" on public.game_sessions;
create policy "game_sessions_update_host"
on public.game_sessions for update
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = game_sessions.room_id
          and p.auth_user_id = auth.uid()
          and p.is_host = true
    )
)
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = game_sessions.room_id
          and p.auth_user_id = auth.uid()
          and p.is_host = true
    )
);

do $$
begin
    if not exists (
        select 1
        from pg_publication
        where pubname = 'supabase_realtime'
    ) then
        raise exception 'supabase_realtime publication does not exist';
    end if;

    if not exists (
        select 1
        from pg_publication_tables
        where pubname = 'supabase_realtime'
          and schemaname = 'public'
          and tablename = 'game_sessions'
    ) then
        alter publication supabase_realtime add table public.game_sessions;
    end if;
end $$;
