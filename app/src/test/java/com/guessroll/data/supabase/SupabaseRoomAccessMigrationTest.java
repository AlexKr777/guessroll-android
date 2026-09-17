package com.guessroll.data.supabase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.Test;

public class SupabaseRoomAccessMigrationTest {

    @Test
    public void authenticatedRoomFlowHasExplicitGrantsAndRlsPolicies() throws IOException {
        File migrations = new File(projectRoot(), "supabase/migrations");
        File migration = Arrays.stream(requireFiles(migrations))
            .filter(file -> file.getName().endsWith("_room_lobby_authenticated_access.sql"))
            .max(Comparator.comparing(File::getName))
            .orElseThrow(() -> new AssertionError(
                "Missing room_lobby_authenticated_access Supabase migration"
            ));

        String sql = new String(Files.readAllBytes(migration.toPath()), StandardCharsets.UTF_8)
            .toLowerCase();
        String executableSql = sql.replaceAll("(?m)--.*$", "");

        assertContains(sql, "grant select, insert, update on table public.rooms to authenticated;");
        assertContains(sql, "grant select, insert, update on table public.players to authenticated;");
        assertContains(sql, "grant select, insert on table public.media_items to authenticated;");
        assertContains(sql, "grant select, insert, update on table public.rounds to authenticated;");
        assertContains(sql, "grant select, insert on table public.guesses to authenticated;");
        assertContains(sql, "grant select, insert, update on table public.game_sessions to authenticated;");
        assertContains(sql, "grant select, insert on table public.round_reactions to authenticated;");

        for (String table : Arrays.asList(
            "rooms",
            "players",
            "media_items",
            "rounds",
            "guesses",
            "game_sessions",
            "round_reactions"
        )) {
            assertContains(sql, "alter table public." + table + " enable row level security;");
        }

        for (String policy : Arrays.asList(
            "rooms_select_authenticated",
            "rooms_insert_authenticated",
            "rooms_update_host",
            "players_select_authenticated",
            "players_insert_self",
            "players_update_self",
            "media_items_select_authenticated",
            "media_items_insert_owner",
            "rounds_select_authenticated",
            "rounds_insert_host",
            "rounds_update_host",
            "guesses_select_authenticated",
            "guesses_insert_self",
            "game_sessions_select_authenticated",
            "game_sessions_insert_host",
            "game_sessions_update_host",
            "round_reactions_select_authenticated",
            "round_reactions_insert_room_player"
        )) {
            assertContains(sql, "drop policy if exists \"" + policy + "\"");
            assertContains(sql, "create policy \"" + policy + "\"");
        }

        assertPolicyContains(
            sql,
            "media_items_select_authenticated",
            "p.room_id = media_items.room_id",
            "p.auth_user_id = (select auth.uid())"
        );
        assertPolicyContains(
            sql,
            "rounds_select_authenticated",
            "p.room_id = rounds.room_id",
            "p.auth_user_id = (select auth.uid())"
        );
        assertPolicyContains(
            sql,
            "guesses_select_authenticated",
            "p.room_id = guesses.room_id",
            "p.auth_user_id = (select auth.uid())"
        );
        assertPolicyContains(
            sql,
            "guesses_insert_self",
            "p.room_id = guesses.room_id",
            "r.id = guesses.round_id",
            "r.room_id = guesses.room_id",
            "gp.id = guesses.guessed_player_id",
            "gp.room_id = guesses.room_id"
        );
        assertPolicyContains(
            sql,
            "game_sessions_select_authenticated",
            "p.room_id = game_sessions.room_id",
            "p.auth_user_id = (select auth.uid())"
        );
        assertPolicyContains(
            sql,
            "round_reactions_select_authenticated",
            "p.room_id = round_reactions.room_id",
            "p.auth_user_id = (select auth.uid())"
        );

        assertFalse("MVP migration must not grant table access to anon", sql.contains(" to anon"));
        assertFalse("Android migration must not grant service_role", sql.contains(" to service_role"));
        assertFalse("Unused DELETE access must stay closed", executableSql.contains("grant delete"));
    }

    private static void assertContains(String sql, String expected) {
        assertTrue("Missing SQL fragment: " + expected, sql.contains(expected));
    }

    private static void assertPolicyContains(String sql, String policyName, String... expectedParts) {
        String marker = "create policy \"" + policyName + "\"";
        int start = sql.indexOf(marker);
        int end = sql.indexOf("drop policy if exists", start + marker.length());
        String policySql = sql.substring(start, end >= 0 ? end : sql.length());
        for (String expected : expectedParts) {
            assertContains(policySql, expected);
        }
    }

    private static File[] requireFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            throw new AssertionError("Cannot read " + directory);
        }
        return files;
    }

    private static File projectRoot() {
        File current = new File(System.getProperty("user.dir"));
        while (current != null && !new File(current, "supabase/migrations").isDirectory()) {
            current = current.getParentFile();
        }
        if (current == null) {
            throw new AssertionError("Cannot locate GuessRoll project root");
        }
        return current;
    }
}
