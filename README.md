# Simple Mod Sync

A lightweight mod for synchronizing game mods via a URL-based schema.

## Features

- **Automated Mod Synchronization**: Synchronize mods from a specified URL on every game start.
- **Customizable Destination**: Choose where mods are downloaded on your system.
- **User-Friendly Setup**: Simple configuration with minimal setup required.

## Example schema file

The file retrieved from the URL must follow this structure:

```json
{
  "sync_version": 2,
  "content": [
    {
      "url": "https://example.com/url/to/mod.jar",
      "mod_name": "some-mod_name",
      "version": "2.8.1",
      "type": "mod"
    }
  ]
}
```
> Add more mods/files as needed to the content array.

## Simple Setup Guide

1. **Create the JSON File**: Use the [example schema](#example-schema-file) to create your mod list file.

2. **Host the File**: Upload the JSON file to an HTTP server. You can use services like [Pastebin](https://pastebin.com) for this.

3. **Install the Mod**: Install the _Simple Mod Sync_ mod as usual. When the game starts for the first time, it will prompt you to enter the URL of your JSON file.

4. **Monitor Synchronization**: Once the URL is set, the mod synchronization status will be visible in the top-left corner of the title screen.

- To change the URL later, simply update the `download_url` setting in the config file.

## For Developers

Want to contribute? Whether it's reporting an issue or submitting a pull request, your help is highly appreciated!

- [Open an Issue](https://github.com/oxydien/simple-mod-sync/issues/new)
- [Create a Pull Request](https://github.com/oxydien/simple-mod-sync/pulls)

## License

This mod is licensed under the MIT License. See the [LICENSE file](./LICENSE) for more details.
