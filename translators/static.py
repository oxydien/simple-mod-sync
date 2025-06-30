#!/bin/python3
"""
© 2025 oxydien | Free to use, share and modify
© 2025 User_green | Free to use, share and modify


This file is part of the simple-mod-sync project.
This script goes through all files in a given directory and parses them to create a sync data file for simple-mod-sync. Used for hosting static content on a server.

Supports:
- .jar
    - Fabric
    - Quilt
    - Forge (NOT BY simple-mod-sync)
    - NeoForge (NOT BY simple-mod-sync)
- .zip
    - Shaders (iris)
    - Data packs (not fully)
    - Resource packs

Usage:
    Modify the configuration at the top of the file to match your needs.

    Run the script with `python3 static.py`
"""

### CONFIGURATION
# Edit this part if needed.

# Base path of the content where it will be hosted
url_base_path = "https://exaple.com/"

# List of directories to parse
working_directories = ["./client_mods/", "./mods/"]

# This file contents will be added to the end of output file.
# Write any MODIFY into .json referenced here.
# Example:
# === #
# {
#  "modify": [
#     {
#      "type": "remove",
#      "pattern": "^ToughAsNail.*$",
#      "path": "./mods"
#     }
#   ]
# }
# === #
#
# Leave empty for no additions
modification_file = "./modifications.json"


# Replace the base path of the content (override for working directories)
working_directory_to_url_override = {
    "./client_mods/":"/mods/", # (ex.: ({url_base_path}/another/directory/) -> ({url_base_path}/backup/))
}

# Location and name of the output file
output_file = "./mod_list.json"

# If the program should try to parse files containing only MANIFEST.MF
# .MF-manifests DO NOT declare platform
# Usualy only coremodes use .MF-only manifests
# Wired things might happen if .MF-only manifests are present
accept_mfs = False



### CODE
# DO NOT EDIT BELOW THIS LINE IF YOU DON'T KNOW WHAT YOU ARE DOING!


import os
import zipfile
import json
import enum
import tomllib as toml
from typing import List


ContentType = enum.Enum('ContentType', ['mod', 'resourcepack', 'datapack', 'shader'])


class Content:
    """
    Represents a piece of content to be synced.

    Attributes:
        url (str): The URL to download the content from.
        name (str): The name of the content.
        version (str): The version of the content.
        type (ContentType): The type of content.
    """
    url: str
    name: str
    version: str
    type: ContentType

    def __init__(self, url: str, name: str, version: str, type: ContentType):
        self.url = url
        self.name = name
        self.version = version
        self.type = type

    def __str__(self):
        return f"Content(url={self.url}, name={self.name}, version={self.version}, type={self.type})"

    def __dict__(self):
        return {
            "url": self.url,
            "name": self.name,
            "version": self.version,
            "type": self.type.name
        }


class SyncData:
    """
    Represents a collection of content to be synced.

    Attributes:
        version (int): The version of the sync data.
        contents (List[Content]): The list of content to be synced.
    """
    version: int
    contents: List[Content]

    def __init__(self, contents: List[Content], version: int = 3):
        self.version = version
        self.contents = contents
        self.archive = None

    def __str__(self):
        return f"SyncData(version={self.version}, contents={self.contents})"

    def add_content(self, content: Content):
        self.contents.append(content)

    def to_dictionary(self):
        return {
            "sync_version": self.version,
            "sync": [content.__dict__() for content in self.contents]
        }


class Parser:
    """
    Parses .jar and .zip files into a Content object based on found manifest.

    Attributes:
        file (str): The path to the file to parse.
        correction (str): The correction to apply to the URL.
        accept_mf (bool): If the program should try to parse files containing only MANIFEST.MF.
    """
    file: str
    correction: str
    accept_mf: bool

    def __init__(self, file_path: str, correction: str, accept_mf: bool = False):
        # Check if the file exists
        if not os.path.exists(file_path):
            raise Exception(f"{file_path} does not exist")

        # Check if the file is a .jar or .zip
        if not file_path.endswith(".jar") and not file_path.endswith(".zip"):
            raise Exception(f"{file_path} is not a .jar or .zip file")

        self.file = file_path
        self.correction = correction
        self.accept_mf = accept_mf

    def parse(self) -> Content:
        """
        Parses the given file into a Content object.

        This method opens the given file as a ZipFile and calls `__parse_manifest` on it.

        :return: A Content object containing the parsed data.
        :raises Exception: If the manifest file type is unknown or if any parsing errors occur.
        """
        with zipfile.ZipFile(self.file, "r") as archive:
            return self.__parse_manifest(archive)

    def __get_manifest(self, archive: zipfile.ZipFile) -> str:
        """
            Gets the path to the manifest of the given archive.

            This method tries the following paths in order:
                - fabric.mod.json
                - quilt.mod.json
                - META-INF/mods.toml (NOTICE: Forge is not supported by simple-mod-sync)
                - META-INF/neoforge.mods.toml (NOTICE: NeoForge is not supported by simple-mod-sync)
                - shaders/
                - data/ (DEV: Might not trigger sometimes)
                - pack.mcmeta

            If none of these paths are found, it raises an exception.

            :param archive: The archive to get the manifest from.
            :return: The path to the manifest (if found).
            :raises Exception: If no manifest is found.
        """
        paths_to_try = [
            "fabric.mod.json",
            "quilt.mod.json",
            "META-INF/mods.toml", # NOTICE: Forge is not supported by simple-mod-sync
            "META-INF/neoforge.mods.toml", # NOTICE: NeoForge is not supported by simple-mod-sync
            "shaders/",
            "data/", # DEV: Might not trigger sometimes
            "pack.mcmeta"
        ]
        for path in paths_to_try:
            try:
                archive.getinfo(path)
                return path
            except:
                pass

        # Check if the program should try to parse files containing only MANIFEST.MF
        if not self.accept_mf:
            raise Exception("No manifest found for the file " + archive.filename + ".")

        # If no manifest was found, try to find a MANIFEST.MF
        manifests = [
            "MANIFEST.MF",
            "META-INF/MANIFEST.MF"
        ]
        for mf in manifests:
            try:
                archive.getinfo(mf)
                return mf
            except:
                pass

        raise Exception("Absolutely no manifest found for the file " + archive.filename + ".")

    def __parse_manifest(self, archive: zipfile.ZipFile) -> Content:
        """
            Parses the manifest file from the given archive and returns a Content object.

            This method identifies the manifest file path using the __get_manifest method.
            It supports various types of content such as shaders, data packs, mods, and
            resource packs. The method parses the manifest file based on its type and
            extracts necessary information to construct a Content object.

            :param archive: The archive from which to parse the manifest.
            :return: Content object representing the parsed content.
            :raises Exception: If the manifest file type is unknown or if any parsing errors occur.
        """
        content: Content
        try:
            path = self.__get_manifest(archive)
            if not path.endswith("/"):
                data_stream = archive.open(path, "r")
            else:
                match (path):

                    # Shaders
                    case "shaders/":
                        uri = self.__get_uri(archive.filename, self.correction)
                        name = self.__get_name_from_path(archive.filename)
                        version = self.__get_rand_version()
                        content = Content(uri, name, version, ContentType.shader)

                    # Data packs
                    case "data/":
                        uri = self.__get_uri(archive.filename, self.correction)
                        name = self.__get_name_from_path(archive.filename)
                        version = self.__get_rand_version()
                        content = Content(uri, name, version, ContentType.datapack)

                    case _:
                        raise Exception(f"Unknown path: {path}")
                return content
        except Exception:
            raise

        # Quilt, Fabric
        if path.endswith(".json"):
            manifest = json.load(data_stream)
            name = manifest["name"]
            version = manifest["version"]
            uri = self.__get_uri(archive.filename, self.correction)
            content = Content(uri, name, version, ContentType.mod)
            return content

        # NeoForge, Forge
        elif path.endswith(".toml"):
            manifest = toml.load(data_stream)['mods'][0]
            name = manifest["displayName"] if "displayName" in manifest else manifest["modId"]
            version = manifest["version"]
            uri = self.__get_uri(archive.filename, self.correction)
            content = Content(uri, name, version, ContentType.mod)

        # Resource packs
        elif path.endswith(".mcmeta"):
            uri = self.__get_uri(archive.filename, self.correction)
            name = self.__get_name_from_path(archive.filename)
            version = self.__get_rand_version()
            content = Content(uri, name, version, ContentType.resourcepack)

        # .MF files (most likely mods)
        elif path.endswith(".MF"):
            uri = self.__get_uri(archive.filename, self.correction)
            name = self.__get_name_from_path(archive.filename)
            version = self.__get_rand_version()
            content = Content(uri, name, version, ContentType.mod)
        else:
            raise Exception(f"Unknown path: {path}")

        data_stream.close()

        return content

    @staticmethod
    def __get_uri(filename: str, correction: str) -> str:
        # Get the relative path of the file
        last_slash_index = filename.rfind("/")
        if last_slash_index != -1:
            relative_path = filename[last_slash_index+1:]
            # If the file is in a subdirectory, remove the subdirectory from the path
            if relative_path.startswith("/"):
                relative_path = relative_path[1:]
        else:
            relative_path = filename

        # Add the correction to the path if it's not empty
        if len(correction) != 0:
            if correction.startswith("/"):
                # Remove the leading slash
                correction = correction[1:]
            if not correction.endswith("/"):
                # Add a trailing slash
                correction = correction + "/"

        # Combine the base URL, the correction and the relative path
        return url_base_path + "/" + correction + relative_path

    @staticmethod
    def __get_name_from_path(path: str) -> str:
        """
            Get the name of the file (/path/to/file.jar -> file)

            :param path: The path to the file
            :return: The name of the file
        """
        return "".join((path.split("/")[-1]).split(".")[:-1])

    @staticmethod
    def __get_rand_version() -> str:
        """
            Generate a random version string.
            :return: 4 random bytes as a string
        """
        return str(int.from_bytes(os.urandom(4), "big"))


def main():
    """
    Goes thought each folder in `working_directories` list.
    For each folder parses .jar and .zip files in the folder and collects
    their content metadata to create a sync data file.

    This function iterates over files in the specified working directory,
    identifies supported file types (.jar and .zip), and uses the Parser
    class to extract relevant information. The extracted content is added
    to a SyncData object, which is then serialized to a JSON file.
    The file is named and placed according to variable "output_file".

    Exceptions during parsing are caught and printed to the console.
    """
    sync_data = SyncData(contents=[])

    for working_directory in working_directories:
        correction = working_directory_to_url_override[working_directory]

        try:
            files = os.listdir(working_directory)
            for file in files:
                if file.endswith(".jar") or file.endswith(".zip"):
                    fixed_path = (working_directory + "/" if not working_directory.endswith("/") else working_directory)
                    file_path = fixed_path + file
                    parser = Parser(file_path, correction, accept_mfs)
                    try:
                        content = parser.parse()
                        sync_data.add_content(content)
                        print(content)
                    except Exception as e:
                        print(e)
        except Exception as e:
            print("Error parsing files in ", working_directory, " ignoring: ", e)

    # HACK, but simple and robus one
    # Dump sync_data into dictionary
    sync_data_dictionary = sync_data.to_dictionary()
    if(len(modification_file) > 3):
        try:
            # Read data from file and convert it into another dictionary
            with open(modification_file, "r") as modification_stream:
                modifications = json.load(modification_stream)
                # Insrt new dict into old one
                sync_data_dictionary.update(modifications)
        except Exception as e:
            print("We were UNABLE to parse modifications file! Error: ", e)


    with open(output_file, "w") as f:
        f.write(str(sync_data_dictionary))

    print("Done")


if __name__ == "__main__":
    main()
