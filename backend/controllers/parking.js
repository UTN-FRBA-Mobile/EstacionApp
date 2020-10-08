const { PARKING_ROOM } = process.env;
const locations = [];

const postParkings = ({ app, body: { latitude, longitude } }, res) => {
  const io = app.get("socketio");

  if (latitude && longitude) {
    locations.push({ latitude, longitude });
    io.to(PARKING_ROOM).emit("locations", locations);
    res.status(200).send({ success: true, data: "Parking added" });
  } else {
    res.status(400).send({ success: false, data: "Wrong body" });
  }
};

module.exports = {
  postParkings,
  locations,
};
