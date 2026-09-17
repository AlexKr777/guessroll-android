-- GuessRoll realtime emoji reactions.
--
-- Safe scope:
-- - creates one append-only reaction event table;
-- - no data deletion;
-- - no table drops;
-- - no game/session/rematch schema changes;
-- - adds round_reactions to Supabase Realtime publication.

create table if not exists public.round_reactions (
    id uuid primary key default gen_random_uuid(),
    room_id uuid not null references public.rooms(id) on delete cascade,
    game_session_id uuid not null references public.game_sessions(id) on delete cascade,
    round_id uuid not null references public.rounds(id) on delete cascade,
    player_id uuid not null references public.players(id) on delete cascade,
    emoji text not null check (emoji in ('😂', '😱', '💀', '👀', '🔥', '🤯')),
    created_at timestamptz not null default now()
);

create index if not exists round_reactions_room_id_idx on public.round_reactions (room_id);
create index if not exists round_reactions_round_created_at_idx on public.round_reactions (round_id, created_at desc);
create index if not exists round_reactions_session_round_idx on public.round_reactions (game_session_id, round_id);

alter table public.round_reactions enable row level security;

grant select, insert on public.round_reactions to authenticated;

drop policy if exists "round_reactions_select_authenticated" on public.round_reactions;
create policy "round_reactions_select_authenticated"
on public.round_reactions for select
to authenticated
using (true);

drop policy if exists "round_reactions_insert_room_player" on public.round_reactions;
create policy "round_reactions_insert_room_player"
on public.round_reactions for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.id = round_reactions.player_id
          and p.room_id = round_reactions.room_id
          and p.auth_user_id = auth.uid()
    )
    and exists (
        select 1
        from public.rounds r
        where r.id = round_reactions.round_id
          and r.room_id = round_reactions.room_id
          and r.game_session_id = round_reactions.game_session_id
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
          and tablename = 'round_reactions'
    ) then
        alter publication supabase_realtime add table public.round_reactions;
    end if;
end $$;
