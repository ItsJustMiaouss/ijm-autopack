## IJM's Autopack

IJM's Autopack is a CLI tool used to automatically update your modpack, without depending on a version of Minecraft.

## Requirements

- [Java 21](https://adoptium.net/fr/temurin/releases/?version=21) or higher.
- [MultiMC](https://multimc.org/) or [PrismLauncher](https://prismlauncher.org/) or [PolyMC](https://polymc.org/) or any other fork.

## Installation

The installation guide will assume you are using PrismLauncher. But you can use any other fork.

- Download the [latest release](https://github.com/ItsJustMiaouss/ijm-autopack/releases).
- Open PrismLauncher or any other fork, then:
  - Select your instance and click on `Folder` (on the right menu).
  - Copy `ijm-autopack-XXX.jar` to the `.minecraft` or `minecraft` (where you have your `saves` and `options.txt`).
  - Copy the name of the downloaded `.jar` file.
- Go back to PrismLauncher:
  - Select your instance and click on `Edit` (on the right menu).
  - Go to `Settings` and on the `Custom commands` tab.
  - In `Pre-launch command:` enter:

``java -jar <copied-jar-name.jar> -gamedir $INST_MC_DIR -host <host-url>``

Replace `<copied-jar-name.jar>` with the name of the downloaded file and `<host-url>` with the URL provided by the server administrator.

![image](https://github.com/user-attachments/assets/3b22ac38-7bae-4c6c-a4dd-c0f8809ba9cc)


## Server setup

You'll need to use your own storage server like Cloudflare R3 or AWS S3 to store the files, and a simple Node.js application (e.g. Cloudflare Workers).

The server must have an endpoint returning a JSON manifest:

```json
{
  "files": [
    {
      "name": "config/client-config.properties",
      "uri": "https://cdn.<your-domain>/config/client-config.properties",
      "checksum": "<MD5-checksum>"
    },
    {
      "name": "mods/my-mod.jar",
      "uri": "https://cdn.<your-domain>/mods/my-mod.jar",
      "checksum": "<MD5-checksum>"
    }
  ]
}
```
