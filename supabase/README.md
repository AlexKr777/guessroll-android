# Supabase Setup

This folder contains reviewable migrations for the GuessRoll MVP backend.

## Required Supabase settings

1. Create a Supabase project.
2. Enable anonymous sign-ins:
   - Authentication -> Sign In / Providers -> Anonymous sign-ins.
3. Apply the migrations in `supabase/migrations` in filename order.
4. Confirm the private storage bucket exists:
   - Bucket name: `game-media`
   - Public: `false`
5. Copy `local.properties.example` to `local.properties`.
6. Fill:
   - `SUPABASE_URL`
   - `SUPABASE_ANON_KEY`

Never put a service role key in the Android app.

## Storage path format

Slice 2 uploads photos to:

```text
rooms/{roomId}/players/{playerId}/{mediaId}.jpg
```

The storage policies in the migration keep the bucket private and limit uploads to the authenticated anonymous user that owns the matching `players.id` path segment.

## RLS note

The MVP still uses broad authenticated read policies for room/player/media metadata so players can see the lobby state. Storage uploads are path-scoped to the current player. This is acceptable for a demo, but a production pass should tighten table reads with room-membership policies or server-side RPC functions.

## Round reactions

`202605290001_round_reactions.sql` adds the append-only `round_reactions` event table for in-round emoji reactions. It is scoped to `room_id`, `game_session_id`, and `round_id`, validates the approved emoji set, and adds the table to `supabase_realtime` so all players can see reactions during the active round.
