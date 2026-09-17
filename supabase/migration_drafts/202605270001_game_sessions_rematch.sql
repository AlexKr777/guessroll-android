-- GuessRoll rematch/game_sessions architecture draft.
-- Review-only until explicitly applied. Do not remote db push without confirmation.
--
-- Goal:
-- - keep the current one-room-one-game MVP working;
-- - introduce a session boundary for future rematch;
-- - prepare Supabase Realtime publication for client-side room sync.

create table if not exists public.game_sessions (
    id uuid primary key default gen_random_uuid(),
    room_id uuid not null references public.rooms(id) on delete cascade,
    status text not null default 'lobby' check (status in ('lobby', 'playing', 'finished')),
    round_count int not null check (round_count between 1 and 50),
    current_round_number int not null default 0,
    started_at timestamptz null,
    ended_at timestamptz null,
    created_at timestamptz not null default now()
);

alter table public.rooms
    add column if not exists current_game_session_id uuid null references public.game_sessions(id) on delete set null;

alter table public.rounds
    add column if not exists game_session_id uuid null references public.game_sessions(id) on delete cascade;

create index if not exists game_sessions_room_id_idx on public.game_sessions (room_id);
create index if not exists game_sessions_room_status_idx on public.game_sessions (room_id, status);
create index if not exists rounds_game_session_id_idx on public.rounds (game_session_id);

-- Backfill a legacy session per room so existing rooms/rounds have a session boundary.
-- This intentionally leaves rounds.game_session_id nullable for now because the current
-- Android app still reads rounds by room_id. A follow-up app slice should read the active
-- room.current_game_session_id before making game_session_id not null.
insert into public.game_sessions (
    room_id,
    status,
    round_count,
    current_round_number,
    started_at,
    ended_at,
    created_at
)
select
    r.id,
    case
        when r.status = 'finished' then 'finished'
        when r.status = 'playing' then 'playing'
        else 'lobby'
    end,
    r.round_count,
    r.current_round_index,
    case when r.status in ('playing', 'finished') then r.created_at else null end,
    case when r.status = 'finished' then now() else null end,
    r.created_at
from public.rooms r
where not exists (
    select 1
    from public.game_sessions gs
    where gs.room_id = r.id
);

update public.rooms r
set current_game_session_id = (
    select gs.id
    from public.game_sessions gs
    where gs.room_id = r.id
    order by gs.created_at desc, gs.id desc
    limit 1
)
where r.current_game_session_id is null;

update public.rounds rd
set game_session_id = r.current_game_session_id
from public.rooms r
where rd.room_id = r.id
  and rd.game_session_id is null
  and r.current_game_session_id is not null;

alter table public.game_sessions enable row level security;

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

-- Enable table changes for Supabase Realtime if the standard publication exists.
-- The Android app keeps polling as fallback, so realtime publication can be reviewed
-- and applied separately.
do $$
declare
    table_name text;
begin
    if exists (select 1 from pg_publication where pubname = 'supabase_realtime') then
        foreach table_name in array array[
            'rooms',
            'players',
            'media_items',
            'rounds',
            'guesses',
            'game_sessions'
        ]
        loop
            if not exists (
                select 1
                from pg_publication_rel pr
                join pg_publication p on p.oid = pr.prpubid
                join pg_class c on c.oid = pr.prrelid
                join pg_namespace n on n.oid = c.relnamespace
                where p.pubname = 'supabase_realtime'
                  and n.nspname = 'public'
                  and c.relname = table_name
            ) then
                execute format('alter publication supabase_realtime add table public.%I', table_name);
            end if;
        end loop;
    end if;
end $$;

-- Follow-up migration after the Android app reads current_game_session_id:
-- 1. Create rounds with game_session_id.
-- 2. Query rounds by game_session_id, not room_id.
-- 3. Replace the existing unique (room_id, round_number) constraint with
--    unique (game_session_id, round_number).
-- 4. Make rounds.game_session_id not null.
-- 5. Reset per-session scores either via session_results or score snapshots,
--    instead of reusing players.score across multiple sessions.
