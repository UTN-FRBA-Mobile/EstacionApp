const router = require("express").Router();
const { getLogin } = require("../controllers/login");

/************
    GET
*************/
router.get("/", getLogin);

/************
  POST
 *************/

module.exports = router;
