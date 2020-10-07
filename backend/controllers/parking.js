const getParkings = ({ io }, res) => {
  io.on("connection", (socket) => {
    socket.emit("locations", [Math.random(), Math.random()]);
  });

  res.status(200).send({ name: "Asd", sarasa: "ASD" });
};

module.exports = {
  getParkings,
};
