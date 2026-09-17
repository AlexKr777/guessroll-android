-- Restore the explicit Data API privileges required by the Android room/lobby flow.
--
-- GuessRoll signs every device in anonymously before database access. Supabase anonymous
-- users carry the `authenticated` Postgres role; the `anon` role is intentionally not
-- granted access here. Grants make each table reachable, while RLS below continues to
-- decide which rows the current auth.uid() may mutate.
--
-- This migration is non-destructive and deliberately does not grant DELETE privileges.

alter table public.rooms enable row level security;
alter table public.players enable row level security;
alter table public.media_items enable row level security;
alter table public.rounds enable row level security;
alter table public.guesses enable row level security;
alter table public.game_sessions enable row level security;
alter table public.round_reactions enable row level security;

grant select, insert, update on table public.rooms to authenticated;
grant select, insert, update on table public.players to authenticated;
grant select, insert on table public.media_items to authenticated;
grant select, insert, update on table public.rounds to authenticated;
grant select, insert on table public.guesses to authenticated;
grant select, insert, update on table public.game_sessions to authenticated;
grant select, insert on table public.round_reactions to authenticated;

-- MVP visibility: a signed-in party client can resolve a room code and read lobby/game
-- metadata. This is intentionally broad for the current schema and must be replaced by
-- membership-scoped reads before production.
drop policy if exists "rooms_select_authenticated" on public.rooms;
create policy "rooms_select_authenticated"
on public.rooms for select
to authenticated
using (true);

drop policy if exists "rooms_insert_authenticated" on public.rooms;
create policy "rooms_insert_authenticated"
on public.rooms for insert
to authenticated
with check ((select auth.uid()) is not null);

-- Only the authenticated owner of the host player row may change room state.
drop policy if exists "rooms_update_host" on public.rooms;
create policy "rooms_update_host"
on public.rooms for update
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = rooms.id
          and p.auth_user_id = (select auth.uid())
          and p.is_host = true
    )
)
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = rooms.id
          and p.auth_user_id = (select auth.uid())
          and p.is_host = true
    )
);

drop policy if exists "players_select_authenticated" on public.players;
create policy "players_select_authenticated"
on public.players for select
to authenticated
using (true);

drop policy if exists "players_insert_self" on public.players;
create policy "players_insert_self"
on public.players for insert
to authenticated
with check (auth_user_id = (select auth.uid()));

drop policy if exists "players_update_self" on public.players;
create policy "players_update_self"
on public.players for update
to authenticated
using (auth_user_id = (select auth.uid()))
with check (auth_user_id = (select auth.uid()));

drop policy if exists "media_items_select_authenticated" on public.media_items;
create policy "media_items_select_authenticated"
on public.media_items for select
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = media_items.room_id
          and p.auth_user_id = (select auth.uid())
    )
);

drop policy if exists "media_items_insert_owner" on public.media_items;
create policy "media_items_insert_owner"
on public.media_items for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.id = media_items.owner_player_id
          and p.room_id = media_items.room_id
          and p.auth_user_id = (select auth.uid())
    )
);

drop policy if exists "rounds_select_authenticated" on public.rounds;
create policy "rounds_select_authenticated"
on public.rounds for select
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = rounds.room_id
          and p.auth_user_id = (select auth.uid())
    )
);

drop policy if exists "rounds_insert_host" on public.rounds;
create policy "rounds_insert_host"
on public.rounds for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = rounds.room_id
          and p.auth_user_id = (select auth.uid())
          and p.is_host = true
    )
);

drop policy if exists "rounds_update_host" on public.rounds;
create policy "rounds_update_host"
on public.rounds for update
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = rounds.room_id
          and p.auth_user_id = (select auth.uid())
          and p.is_host = true
    )
)
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = rounds.room_id
          and p.auth_user_id = (select auth.uid())
          and p.is_host = true
    )
);

drop policy if exists "guesses_select_authenticated" on public.guesses;
create policy "guesses_select_authenticated"
on public.guesses for select
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = guesses.room_id
          and p.auth_user_id = (select auth.uid())
    )
);

drop policy if exists "guesses_insert_self" on public.guesses;
create policy "guesses_insert_self"
on public.guesses for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.id = guesses.player_id
          and p.room_id = guesses.room_id
          and p.auth_user_id = (select auth.uid())
    )
    and exists (
        select 1
        from public.rounds r
        where r.id = guesses.round_id
          and r.room_id = guesses.room_id
          and r.game_session_id = guesses.game_session_id
    )
    and exists (
        select 1
        from public.players gp
        where gp.id = guesses.guessed_player_id
          and gp.room_id = guesses.room_id
    )
);

drop policy if exists "game_sessions_select_authenticated" on public.game_sessions;
create policy "game_sessions_select_authenticated"
on public.game_sessions for select
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = game_sessions.room_id
          and p.auth_user_id = (select auth.uid())
    )
);

drop policy if exists "game_sessions_insert_host" on public.game_sessions;
create policy "game_sessions_insert_host"
on public.game_sessions for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = game_sessions.room_id
          and p.auth_user_id = (select auth.uid())
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
          and p.auth_user_id = (select auth.uid())
          and p.is_host = true
    )
)
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = game_sessions.room_id
          and p.auth_user_id = (select auth.uid())
          and p.is_host = true
    )
);

drop policy if exists "round_reactions_select_authenticated" on public.round_reactions;
create policy "round_reactions_select_authenticated"
on public.round_reactions for select
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = round_reactions.room_id
          and p.auth_user_id = (select auth.uid())
    )
);

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
          and p.auth_user_id = (select auth.uid())
    )
    and exists (
        select 1
        from public.rounds r
        where r.id = round_reactions.round_id
          and r.room_id = round_reactions.room_id
          and r.game_session_id = round_reactions.game_session_id
    )
);
