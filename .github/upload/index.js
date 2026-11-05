import * as fs from "fs";

(async () => {
    try {
        const file = fs.readdirSync("../../build/libs", {withFileTypes: true}).at(0);
        const blob = await fs.openAsBlob(`${file.parentPath}/${file.name}`);
        const form = new FormData();
        form.append("file", blob, file.name);
        form.append("hash", process.argv[2]);
        form.append("version", file.name.split("-").at(1));
        form.append("branch", process.argv[3]);
        form.append("message", process.argv[4]);
        const res = await fetch("https://whatyouth.ing/api/nofrills/v1/misc/post-beta-build", {
            method: "POST",
            headers: {
                "NF-Beta-Auth": process.env.NF_API_BETA_AUTH
            },
            body: form
        })
        console.log(`Response code: ${res.status} (${blob.size} upload size)`);
        console.log(`Build path: ${file.parentPath}/${file.name}`);
    } catch (error) {
        console.error('Error:', error);
    }
})();