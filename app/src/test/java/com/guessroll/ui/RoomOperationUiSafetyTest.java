package com.guessroll.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Test;

public class RoomOperationUiSafetyTest {

    @Test
    public void roomFailuresAreLoggedButTechnicalDetailsStayOutOfUi() throws IOException {
        File sourceFile = new File(
            projectRoot(),
            "app/src/main/java/com/guessroll/ui/GuessRollViewModel.kt"
        );
        String source = new String(Files.readAllBytes(sourceFile.toPath()), StandardCharsets.UTF_8);
        int operationStart = source.indexOf("private fun launchRoomOperation");
        int operationEnd = source.indexOf("private fun showError", operationStart);
        assertTrue("Cannot locate launchRoomOperation", operationStart >= 0 && operationEnd > operationStart);
        String roomOperationSource = source.substring(operationStart, operationEnd);

        assertFalse(
            "Throwable messages can contain PostgREST URLs, codes, and authorization details",
            roomOperationSource.contains("errorMessage = throwable.message")
        );
        assertTrue(source.contains("Не удалось создать комнату. Попробуй ещё раз."));
        assertTrue(source.contains("Не удалось войти в комнату. Проверь код и попробуй ещё раз."));
        assertTrue(roomOperationSource.contains("Log.e(RoomOperationLogTag"));
    }

    private static File projectRoot() {
        File current = new File(System.getProperty("user.dir"));
        while (current != null && !new File(current, "app/src/main").isDirectory()) {
            current = current.getParentFile();
        }
        if (current == null) {
            throw new AssertionError("Cannot locate GuessRoll project root");
        }
        return current;
    }
}
