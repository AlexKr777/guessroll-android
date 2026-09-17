-- Enable Supabase Realtime change events for the current GuessRoll MVP tables.
--
-- Safe scope:
-- - publication-only changes;
-- - no table creation;
-- - no backfill;
-- - no RLS changes;
-- - no destructive operations.
--
-- Client subscriptions currently listen to:
-- - rooms
-- - players
-- - media_items
-- - rounds
--
-- guesses is intentionally not added here because the app does not subscribe to
-- guesses directly. Score/result freshness arrives through players/rooms/rounds
-- refreshes, with polling as fallback.

do $$
declare
    table_name text;
begin
    if not exists (
        select 1
        from pg_publication
        where pubname = 'supabase_realtime'
    ) then
        raise exception 'supabase_realtime publication does not exist';
    end if;

    foreach table_name in array array[
        'rooms',
        'players',
        'media_items',
        'rounds'
    ]
    loop
        if not exists (
            select 1
            from pg_publication_tables
            where pubname = 'supabase_realtime'
              and schemaname = 'public'
              and tablename = table_name
        ) then
            execute format('alter publication supabase_realtime add table public.%I', table_name);
        end if;
    end loop;
end $$;
