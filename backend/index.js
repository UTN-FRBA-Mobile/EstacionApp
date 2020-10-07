const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");
const app = express();
const socket = require("socket.io");
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

const socketMiddleware = (req, res, next) => {
  req.io = io;
  next();
};

app
  .use(cors(corsOptions))
  .use(bodyParser.json())
  .use(bodyParser.urlencoded({ extended: false }))
  .use(socketMiddleware)
  .use("/parkings", require("./routes/parking.js"))
  .use("/login", require("./routes/login.js"))
  .use("*", respondWith404);

const server = app.listen(PORT || "5000", () => {
  console.log(`Example app listening at http://localhost:${PORT || "5000"}`);
});

const io = socket(server);