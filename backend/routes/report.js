const router = require("express").Router();
const multer = require("multer");
const { postReport, getReports } = require("../controllers/report");

upload = multer({ dest: "uploads/" });

router.post("/", upload.single("image"), postReport);

router.get("/", getReports);

module.exports = router;
