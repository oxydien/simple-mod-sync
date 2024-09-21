# Simple Mod Sync Translators

## Overview

This is a collection of scripts that translate modpacks from various formats to the Simple Mod Sync format. 

### Currently Supported Translators

- [Modrinth app](#modrinth-app-translator)
    - Script: `index.js`

### Contribute translator

If you want to help translate the modpacks, please [open an issue](https://github.com/oxydien/simple-mod-sync/issues/new) or [create a pull request](https://github.com/oxydien/simple-mod-sync/pulls).

This will be highly appreciated.

## Modrinth App Translator

The Modrinth app translator converts modpack instances from the Modrinth format to the Simple Mod Sync format.

### Prerequisites

- Node.js installed on your system
- Git (for cloning the repository)

### Initial setup

1. Clone the repository:
   ```
   git clone https://github.com/oxydien/simple-mod-sync.git
   cd simple-mod-sync
   ```

2. Navigate to the `translators` folder:
   ```
   cd translators
   ```

3. Create an `input` directory:
   ```
   mkdir input
   ```

### Usage

1. Open the Modrinth app and navigate to the content page of the instance you want to translate.

2. Open the Developer Tools:
    - Windows/Linux: Press `Ctrl + Shift + I`
    - macOS: Press `Cmd + Option + I`

3. Switch to the "Network" tab in the Developer Tools.

4. Refresh the page:
    - Windows/Linux: Press `Ctrl + Shift + R`
    - macOS: Press `Cmd + Option + R`

5. In the network request list, find the request containing `get_version_many`:
    - Click on the request
    - Copy the content of the response
    - Save it in the `input` folder as `input.json`

6. Find the request containing `get_project_many`:
    - Click on the request
    - Copy the content of the response
    - Save it in the `input` folder as `input-projects.json`

7. Run the translator script:
   ```
   node index.js
   ```

8. The translated output will be generated in the `output` folder.

### Notes

- Input file names are flexible:
    - The main input file must end with `.json`
    - The projects file must end with `-projects.json`
- You can translate multiple instances simultaneously by including multiple pairs of input files in the `input` folder.

## Output

The translator will generate files in the Simple Mod Sync format in the `output` folder.

## Troubleshooting

If you encounter any issues:
1. Ensure you have the latest version of Node.js installed
2. Check that the input JSON files are valid and complete
3. Verify that you have write permissions in the `output` folder
