/**
 * @author oxydien
 * @copyright 2024
 * @description Translates Modrinth's versions to the json file format of 'Simple Mod Sync'
 * @version 1.0.1
 * @license MIT
 */

// Read the README.md file to understand how to use this script.

import fs from 'fs';
import { fileURLToPath } from 'url';
import path from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

console.warn("Modrinth => Simple Mod Sync translator");
console.log("Translates Modrinth's versions to the json file format of 'Simple Mod Sync'");

/**
 * Parses a string name to remove special characters.
 * @param {string} stringName - The name to parse.
 * @returns {string} The parsed name.
 */
const parseName = (stringName) => {
	return stringName.replace(/[^a-zA-Z0-9.\-_\[\]]/g, "");
};

/**
 * Determines the file type based on the filename.
 * @param {string} filename - The filename to check.
 * @returns {string|null} The file type or null if undetermined.
 */
const getFileType = (filename) => {
	if (filename.endsWith(".jar")) return "mod";
	if (filename.endsWith(".zip")) return "resourcepack";
	return null;
};

/**
 * Translates Modrinth's versions to the json file format of 'Simple Mod Sync'
 */
const translate = async () => {
	console.log("Checking input directory...");
	const inputDir = path.join(__dirname, "input");

	console.log("Input directory found, checking files...");
	const files = fs.readdirSync(inputDir);

	if (files.length === 0) {
		console.error("Input directory is empty");
		process.exit(1);
	}

	for (const file of files) {
		if (!file.endsWith(".json") || file.endsWith("-projects.json")) continue;

		console.log(`Translating ${file}...`);
		const filePath = path.join(inputDir, file);
		const data = JSON.parse(fs.readFileSync(filePath, "utf-8"));

		if (!Array.isArray(data) || data.length === 0) {
			console.error(`${file} is not a valid array`);
			continue;
		}

		console.log(`Loaded ${data.length} versions from ${file}, checking for projects file...`);
		const projectFile = file.replace(".json", "-projects.json");
		const projectFilePath = path.join(inputDir, projectFile);
		let projectData = null;

		try {
			const fileContent = fs.readFileSync(projectFilePath, "utf-8");
			projectData = JSON.parse(fileContent);
			console.log("Loaded project data from " + projectFile);
		} catch (error) {
			console.error(`Project file '${projectFile}' not found, proceeding without project data...`);
		}

		const content = data.reduce((acc, version) => {
			if (!version.id || !version.project_id || !version.name || version.files.length === 0 || !version.files[0].url) {
				console.error(`Invalid version data in ${file} for version ${version.id}`);
				return acc;
			}

			const project = projectData ? projectData.find(p => p.id === version.project_id) : null;
			const fileType = getFileType(version.files[0].filename);

			acc.push({
				mod_name: project ? project.slug : parseName(version.name),
				version: version.id,
				type: project ? project.project_type : fileType,
				url: version.files[0].url,
			});

			console.log(`Translated ${file} to ${version.id}`);
			return acc;
		}, []);

		console.log(`Writing output file for ${file}...`);
		const output = { sync_version: 1, content };
		const outputJson = JSON.stringify(output, null, 2);

		fs.mkdirSync("output", { recursive: true });
		fs.writeFileSync(`output/${file}`, `${outputJson}\n`);
	}
};

translate()
	.then(() => console.log("Done!"))
	.catch(error => console.error("An error occurred:", error));
