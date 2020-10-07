const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");
const app = express();
require("dotenv").config();

const { PORT } = process.env;

const respondWith404 = (_, res) => {
  res.status(404).json({ success: false, data: "Endpoint not found" });
};

var whitelist = [""];
var corsOptions = {
  origin: function (origin, callback) {
    if (origin === undefined || whitelist.indexOf(origin) !== -1) {
      callback(null, true);
    } else {
      callback(new Error("Not allowed by CORS " + origin));
    }
  },
};

app
  .use(cors(corsOptions))
  .use(bodyParser.json())
  .use(bodyParser.urlencoded({ extended: false }))
  .use("/login", require("./routes/login.js"))
  .use("*", respondWith404);

app.listen(PORT, () => {
  console.log(`Example app listening at http://localhost:${PORT}`);
});

module.exports = app;
