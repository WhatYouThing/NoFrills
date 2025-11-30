import * as fs from "fs";

const file = fs.readdirSync("../../build/libs", { withFileTypes: true }).at(0);
const bytes = fs.readFileSync(`${file.parentPath}/${file.name}`);
const body = {
    "bytes": bytes,
    "hash": process.argv[2],
    "version": file.name.split("-").at(1),
    "message": process.argv[3]
};

fetch("https://whatyouth.ing/api/nofrills/v1/misc/post-beta-build", {
    method: "POST",
    headers: {
        "nf-beta-auth": process.env.NF_API_BETA_AUTH,
    },
    body: JSON.stringify(body)
}).then(res => {
    console.log(`Response code: ${res.status}`);
});
console.log(`Build path: ${file.parentPath}/${file.name}`);