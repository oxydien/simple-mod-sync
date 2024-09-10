# Simple Mod Sync

A simple mod that synchronizes mods based on a URL with a specific schema.

## How to use

1. Set the `download_url` setting in the config file or wait the game to ask you to the URL of the mod list you want to use.
2. Configure the `download_destination` (if needed) setting to the directory where you want the mods to be downloaded.

## Example schema file

This file has to be returned when requesting the given URL

```json
{
  "sync_version": 1,
  "content": [
    {
      "url": "https://example.com/url/to/mod.jar",
      "mod_name": "mod",
      "version": "2.8.1"
    }
    // more mods if needed
  ]
}
```
