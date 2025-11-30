import * as fs from "fs";
import * as path from "path";

const file = fs.readdirSync("../../build/libs", { withFileTypes: true }).at(0);
const bytes = fs.readFileSync(`${file.parentPath}/${file.name}`);
const form = new FormData();

form.set("bytes", new Blob([bytes], { type: "application/java-archive" }), path.basename(`${file.parentPath}/${file.name}`));
form.set("hash", process.argv[2]);
form.set("version", file.name.split("-").at(1));
form.set("message", process.argv[3]);

fetch("https://whatyouth.ing/api/nofrills/v1/misc/post-beta-build", {
    method: "POST",
    headers: {
        "nf-beta-auth": process.env.NF_API_BETA_AUTH,
    },
    body: form
}).then(res => {
    console.log(`Response code: ${res.status}`);
});
console.log(`Build path: ${file.parentPath}/${file.name}`);