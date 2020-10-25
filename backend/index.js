const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");
const app = express();
const socket = require("socket.io");
require("dotenv").config();
const { locations } = require("./controllers/parking");

const { PORT } = process.env;

const respondWith404 = (_, res) => {
  res.status(404).json({ success: false, data: "Endpoint not found" });
};

var whitelist = [""];
var corsOptions = {
  origin: function (origin, callback) {
    callback(null, true);
  },
};

app
  .use(cors(corsOptions))
  .use(bodyParser.json())
  .use(bodyParser.urlencoded({ extended: false }))
  .use("/parkings", require("./routes/parking.js"))
  .use("/login", require("./routes/login.js"))
  .use("*", respondWith404);

const server = app.listen(PORT || "5000", () => {
  console.log(`Example app listening at http://localhost:${PORT || "5000"}`);
});

/********
SOCKET IO
*********/

const io = socket(server, { transports: ["websocket"] });
app.set("socketio", io);

io.on("connection", (socket) => {
  console.log("Connect");
  const { room } = socket.handshake.query;

  if (!room) socket.disconnect();
  else socket.join(room);

  socket.on("connect_error", function (err) {
    console.log(err);
  });

  const availableLocations = locations.filter((location) => !location.reserved);

  socket.emit("initial_locations", availableLocations);

  socket.on("reserve_location", ({ latitude, longitude }) => {
    if (!latitude || !longitude) return;
    const deletedLocation = locations.find(
      (location) =>
        location.latitude === +latitude && location.longitude === +longitude
    );
    if (!deletedLocation) return;
    deletedLocation.reserved = true;
  });

  socket.on("error", function (err) {
    console.log(err);
  });
});
