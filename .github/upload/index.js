import { readdirSync, readFileSync } from "fs";

const hash = process.argv[2];
const commitMessage = process.argv[3];

const file = readdirSync("../../build/libs", { withFileTypes: true }).at(0);
fetch(`https://whatyouth.ing/api/nofrills/v1/misc/post-beta-build`, {
    method: "POST",
    headers: {
        "nf-auth": process.env.NF_API_BETA_AUTH,
        "nf-beta-hash": hash,
        "nf-beta-msg": commitMessage,
        "nf-beta-ver": file.name.split("-").at(1)
    },
    body: readFileSync(`${file.parentPath}/${file.name}`)
}).then(res => {
    console.log(`Response code: ${res.status}`);
});
console.log(`Build path: ${file.parentPath}/${file.name}`);