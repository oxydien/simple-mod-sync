/**
 * @author oxydien
 * @copyright 2024
 * @description Translates Modrinth's versions to the json file format of 'Simple Mod Sync'
 * @version 1.0.0
 * @license MIT
 */

console.warn("Modrinth => Simple Mod Sync translator");
console.log(
	"Translates Modrinth's versions to the json file format of 'Simple Mod Sync'",
);

console.log("Loading dependencies...");
const fs = require("node:fs");
const path = require("node:path");

/**
 * Translates Modrinth's versions to the json file format of 'Simple Mod Sync'
 *
 * Scans the input directory for .json files, and translates them to the
 * format required by Simple Mod Sync. The translated files are written to
 * the output directory.
 *
 * Required file structure:
 * - input/MODS_VERSIONS.json
 * - input/MODS_VERSIONS-projects.json
 *
 * The MODS_VERSIONS.json file must be an array of objects with the following:
 * - id
 * - project_id
 * - name
 * - files
 *   - url
 *   - filename
 *
 * The MODS_VERSIONS-projects.json file must be an array of objects with the following:
 * - id
 * - slug
 * - project_type
 *
 * @returns {void}
 */
const translate = () => {
	console.log("Checking input directory...");

	const dir = path.join(__dirname, "input");

	if (!fs.existsSync(dir)) {
		console.error("Input directory not found");
		process.exit(1);
	}

	console.log("Input directory found, checking files...");

	const files = fs.readdirSync(dir);

	if (files.length === 0) {
		console.error("Input directory is empty");
		process.exit(1);
	}

	for (const file of files) {
		if (!file.endsWith(".json")) {
			console.error("Input directory contains files that are not .json");
			continue;
		}

		if (file.endsWith("-projects.json")) {
			continue;
		}

		console.log(`Translating ${file}...`);
		const filePath = path.join(dir, file);
		const json = fs.readFileSync(filePath, "utf-8");

		const data = JSON.parse(json);

		if (!Array.isArray(data) || data.length === 0) {
			console.error(`${file} is not an array`);
			continue;
		}

		console.log(
			`Loaded ${data.length} versions from ${file}, checking for projects file...`,
		);
		let projectData = null;
		const projectFile = file.replace(".json", "-projects.json");
		if (fs.existsSync(path.join(dir, projectFile))) {
			const projectFilePath = path.join(dir, projectFile);
			const projectJson = fs.readFileSync(projectFilePath, "utf-8");
			projectData = JSON.parse(projectJson);
		} else {
			console.error(
				`Project file '${projectFile}' not found, trying without project data...`,
			);
		}

		const content = [];

		for (const version of data) {
			if (!version.id || !version.project_id) {
				console.error(`${file} contains a version without an id or project_id`);
				continue;
			}
			let project = null;
			if (projectData) {
				project = projectData.find((p) => p.id === version.project_id);
				if (!project) {
					console.error(
						`Project ${version.project_id} not found in project file`,
					);
				}
			}

			if (!version.name) {
				console.error(
					`${file} contains a version ${version.id} without a name`,
				);
				continue;
			}

			if (version.files.length === 0) {
				console.error(`${file} contains a version ${version.id} without files`);
				continue;
			}

			if (!version.files[0].url) {
				console.error(`${file} contains a version ${version.id} without a url`);
				continue;
			}

			let fileType = null;

			if (version.files[0].filename) {
				if (version.files[0].filename.endsWith(".jar")) {
					fileType = "mod";
				} else if (version.files[0].filename.endsWith(".zip")) {
					fileType = "resourcepack";
				}
			}

			content.push({
				mod_name: project ? project.slug : parseName(version.name),
				version: version.id,
				type: project ? project.project_type : fileType,
				url: version.files[0].url,
			});

			console.log(`Translated ${file} to ${version.id}`);
		}

		console.log(`Writing output file for ${file}...`);

		const output = {
			sync_version: 1,
			content,
		};

		const outputJson = JSON.stringify(output, null, 2);

		fs.mkdirSync("output", { recursive: true });
		fs.writeFileSync(`output/${file}`, `${outputJson}\n`);
	}
};

const parseName = (stringName) => {
	return stringName.replace(/[^a-zA-Z0-9.\-_\[\]]/g, "");
};

translate();
console.log("Done!");
