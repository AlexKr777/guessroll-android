-- GuessRoll MVP schema draft.
-- Apply this in a Supabase SQL editor or through Supabase CLI after reviewing the RLS notes.
-- This migration is intentionally non-destructive: it only creates missing objects/policies.

create extension if not exists "pgcrypto";

create table if not exists public.rooms (
    id uuid primary key default gen_random_uuid(),
    code text unique not null,
    host_player_id uuid null,
    mode text not null default 'photo' check (mode in ('photo', 'video')),
    round_count int not null check (round_count between 3 and 50),
    status text not null default 'lobby' check (status in ('lobby', 'uploading', 'playing', 'finished')),
    current_round_index int not null default 0,
    created_at timestamptz not null default now()
);

create table if not exists public.players (
    id uuid primary key default gen_random_uuid(),
    room_id uuid not null references public.rooms(id) on delete cascade,
    auth_user_id uuid not null default auth.uid(),
    nickname text not null check (char_length(trim(nickname)) between 2 and 18),
    is_host boolean not null default false,
    score int not null default 0,
    joined_at timestamptz not null default now()
);

alter table public.rooms
    add constraint rooms_host_player_id_fkey
    foreign key (host_player_id) references public.players(id) on delete set null;

create table if not exists public.media_items (
    id uuid primary key default gen_random_uuid(),
    room_id uuid not null references public.rooms(id) on delete cascade,
    owner_player_id uuid not null references public.players(id) on delete cascade,
    storage_path text not null,
    media_type text not null default 'photo' check (media_type in ('photo', 'video')),
    created_at timestamptz not null default now()
);

create table if not exists public.rounds (
    id uuid primary key default gen_random_uuid(),
    room_id uuid not null references public.rooms(id) on delete cascade,
    media_item_id uuid not null references public.media_items(id) on delete cascade,
    correct_player_id uuid not null references public.players(id) on delete cascade,
    round_number int not null,
    status text not null default 'pending' check (status in ('pending', 'active', 'finished')),
    unique (room_id, round_number)
);

create table if not exists public.guesses (
    id uuid primary key default gen_random_uuid(),
    round_id uuid not null references public.rounds(id) on delete cascade,
    player_id uuid not null references public.players(id) on delete cascade,
    guessed_player_id uuid not null references public.players(id) on delete cascade,
    is_correct boolean not null,
    created_at timestamptz not null default now(),
    unique (round_id, player_id)
);

create index if not exists rooms_code_idx on public.rooms (code);
create index if not exists players_room_id_idx on public.players (room_id);
create index if not exists players_auth_user_id_idx on public.players (auth_user_id);
create index if not exists media_items_room_id_idx on public.media_items (room_id);
create index if not exists rounds_room_id_idx on public.rounds (room_id);
create index if not exists guesses_round_id_idx on public.guesses (round_id);

alter table public.rooms enable row level security;
alter table public.players enable row level security;
alter table public.media_items enable row level security;
alter table public.rounds enable row level security;
alter table public.guesses enable row level security;

-- MVP policy note:
-- The app uses Supabase anonymous sign-in, so runtime requests use the authenticated role.
-- Room-code games need players to see other players in the same room. Without a custom server,
-- these policies are intentionally broad for signed-in anonymous users and should be tightened
-- in a production pass with room membership checks and/or server-side RPC functions.

create policy "rooms_select_authenticated"
on public.rooms for select
to authenticated
using (true);

create policy "rooms_insert_authenticated"
on public.rooms for insert
to authenticated
with check (true);

create policy "rooms_update_host"
on public.rooms for update
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = rooms.id
          and p.auth_user_id = auth.uid()
          and p.is_host = true
    )
)
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = rooms.id
          and p.auth_user_id = auth.uid()
          and p.is_host = true
    )
);

create policy "players_select_authenticated"
on public.players for select
to authenticated
using (true);

create policy "players_insert_self"
on public.players for insert
to authenticated
with check (auth_user_id = auth.uid());

create policy "players_update_self"
on public.players for update
to authenticated
using (auth_user_id = auth.uid())
with check (auth_user_id = auth.uid());

create policy "media_items_select_authenticated"
on public.media_items for select
to authenticated
using (true);

create policy "media_items_insert_owner"
on public.media_items for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.id = media_items.owner_player_id
          and p.room_id = media_items.room_id
          and p.auth_user_id = auth.uid()
    )
);

create policy "rounds_select_authenticated"
on public.rounds for select
to authenticated
using (true);

create policy "rounds_insert_host"
on public.rounds for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = rounds.room_id
          and p.auth_user_id = auth.uid()
          and p.is_host = true
    )
);

create policy "rounds_update_host"
on public.rounds for update
to authenticated
using (
    exists (
        select 1
        from public.players p
        where p.room_id = rounds.room_id
          and p.auth_user_id = auth.uid()
          and p.is_host = true
    )
)
with check (
    exists (
        select 1
        from public.players p
        where p.room_id = rounds.room_id
          and p.auth_user_id = auth.uid()
          and p.is_host = true
    )
);

create policy "guesses_select_authenticated"
on public.guesses for select
to authenticated
using (true);

create policy "guesses_insert_self"
on public.guesses for insert
to authenticated
with check (
    exists (
        select 1
        from public.players p
        where p.id = guesses.player_id
          and p.auth_user_id = auth.uid()
    )
);

insert into storage.buckets (id, name, public)
values ('game-media', 'game-media', false)
on conflict (id) do nothing;

create policy "game_media_select_room_players"
on storage.objects for select
to authenticated
using (
    bucket_id = 'game-media'
    and (storage.foldername(name))[1] = 'rooms'
    and exists (
        select 1
        from public.players p
        where p.room_id::text = (storage.foldername(name))[2]
          and p.auth_user_id = auth.uid()
    )
);

create policy "game_media_insert_owner"
on storage.objects for insert
to authenticated
with check (
    bucket_id = 'game-media'
    and (storage.foldername(name))[1] = 'rooms'
    and (storage.foldername(name))[3] = 'players'
    and exists (
        select 1
        from public.players p
        where p.room_id::text = (storage.foldername(name))[2]
          and p.id::text = (storage.foldername(name))[4]
          and p.auth_user_id = auth.uid()
    )
);

create policy "game_media_update_owner"
on storage.objects for update
to authenticated
using (
    bucket_id = 'game-media'
    and (storage.foldername(name))[1] = 'rooms'
    and (storage.foldername(name))[3] = 'players'
    and exists (
        select 1
        from public.players p
        where p.room_id::text = (storage.foldername(name))[2]
          and p.id::text = (storage.foldername(name))[4]
          and p.auth_user_id = auth.uid()
    )
)
with check (
    bucket_id = 'game-media'
    and (storage.foldername(name))[1] = 'rooms'
    and (storage.foldername(name))[3] = 'players'
    and exists (
        select 1
        from public.players p
        where p.room_id::text = (storage.foldername(name))[2]
          and p.id::text = (storage.foldername(name))[4]
          and p.auth_user_id = auth.uid()
    )
);
