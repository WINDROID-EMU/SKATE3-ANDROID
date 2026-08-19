package chat.buku.skate3;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class TitleActivity extends Activity {
    private static final int REQUEST_ISO = 1001;
    private File gameDirectory;
    private File storageRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        File external = getExternalFilesDir(null);
        storageRoot = external != null ? external : getFilesDir();
        gameDirectory = new File(storageRoot, "game");

        Button startButton = findViewById(R.id.startButton);
        Button exitButton = findViewById(R.id.exitButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGameReady()) {
                    // Game is already installed, launch directly
                    launchGame();
                } else {
                    // Game not installed, open ISO picker
                    pickIso();
                }
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean isGameReady() {
        try {
            Path root = gameDirectory.toPath();
            return Files.isRegularFile(root.resolve("default.xex")) &&
                   Files.isRegularFile(root.resolve("data/webkit/EAWebkit.xex")) &&
                   TitleUpdateInstaller.isInstalled(root);
        } catch (Exception e) {
            return false;
        }
    }

    private void launchGame() {
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }

    private void pickIso() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_ISO);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, "Android could not open its file picker.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ISO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
            }
            
            // Launch the main LauncherActivity with the selected ISO
            Intent launcherIntent = new Intent(this, LauncherActivity.class);
            launcherIntent.setData(uri);
            startActivity(launcherIntent);
            finish();
        }
    }
}
