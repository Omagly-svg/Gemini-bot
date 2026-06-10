I added a GitHub Actions workflow to build the mod and upload the produced JAR(s) as artifacts. Once the workflow finishes, you can download them from the Actions run's Artifacts section.

Notes:
- The workflow runs on pushes to main and can also be triggered manually via "Run workflow" on the Actions page.
- If you want the workflow to build on PRs or other branches, tell me and I will update the triggers.
- After the artifact is downloaded, put the regular (non-sources) JAR into your Minecraft mods folder. Also make sure to add Baritone's JAR to the same mods folder at runtime if you want Baritone commands to work.
