const { report } = require("../routes/report");
const reports = [];

const postReport = ({ file, body: { user_id } }, res) => {
  if (!file || !user_id) {
    res.status(400).send({ success: false, data: "Wrong body" });
    return;
  }
  reports.push({ image: file, user_id, date: new Date().toISOString() });
  res.status(201).send({ success: true, data: "Report added" });
};

const getReports = (req, res) => {
  res.status(200).send({ reports });
};

module.exports = {
  postReport,
  getReports,
};
