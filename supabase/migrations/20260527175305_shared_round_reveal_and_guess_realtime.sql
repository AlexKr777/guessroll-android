-- Shared round reveal and room-scoped guess realtime for GuessRoll.
--
-- Safe scope:
-- - no data deletion;
-- - no table drops;
-- - no storage changes;
-- - extends round status with a new `revealed` state;
-- - adds `guesses.room_id` so Realtime can be filtered by room;
-- - adds guesses to the supabase_realtime publication idempotently.

alter table public.rounds
    drop constraint if exists rounds_status_check;

alter table public.rounds
    add constraint rounds_status_check
    check (status in ('pending', 'active', 'revealed', 'finished'));

alter table public.guesses
    add column if not exists room_id uuid;

update public.guesses g
set room_id = r.room_id
from public.rounds r
where g.round_id = r.id
  and g.room_id is null;

alter table public.guesses
    alter column room_id set not null;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'guesses_room_id_fkey'
          and conrelid = 'public.guesses'::regclass
    ) then
        alter table public.guesses
            add constraint guesses_room_id_fkey
            foreign key (room_id) references public.rooms(id) on delete cascade;
    end if;
end $$;

create index if not exists guesses_room_id_idx on public.guesses (room_id);

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
          and tablename = 'guesses'
    ) then
        alter publication supabase_realtime add table public.guesses;
    end if;
end $$;
