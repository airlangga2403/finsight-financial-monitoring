# FinSight Newman

Install:
npm install -g newman
npm install -g newman-reporter-htmlextra

Run:
newman run .\FinSight_Real_Backend_Collection.postman_collection.json `
-e .\FinSight_Real_Backend_Environment.postman_environment.json `
-r "cli,htmlextra" `
--reporter-htmlextra-export .\newman-report.html
