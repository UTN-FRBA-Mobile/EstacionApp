const postParkings = ({ io, body: { latitude, longitude } }, res) => {
  if (latitude && longitude) {
    locations.push({ latitude, longitude });
    io.emit("locations", [Math.random(), Math.random()]);
  }
};

module.exports = {
  postParkings,
};
