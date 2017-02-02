package load
import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LoadScript extends Simulation{
  val users = ssv("C:\\test_users.csv").circular
  val httpConf = http
    .baseURL("https://www.tinkoff.ru/ ")
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0")

  val basicLoad = scenario("BASIC_LOAD").feed(users).during(20 minutes) {
    exec(BasicLoad.start)
  }
  setUp(
    basicLoad.inject(rampUsers(10) over (20 seconds))
      .protocols(httpConf))
    .maxDuration(21 seconds)

}

object BasicLoad {

  def getTen = 10
  val start =
    exec(
      http("HTTP Request auth")
        .post("/rest/session-start")
        .formParam("login", "${login}")
        .formParam("password", "${password}")
        .check(status is 200)
    )
      .exec(
        http("HTTP Request getSkills")
          .get("/rest/skills")
          .check(status is 200, jsonPath("$.id").saveAs("idSkill"))
      )
      .exec(
        http("HTTP Request getResults")
          .get("/rest/results")
          .check(status is 200, jsonPath("$.id").saveAs("idResult"))
      )
      .repeat(15) {
        exec(session => {
          println("Some Log")
          val tmp = getTen
          session.set("ten",tmp)
        })
          .exec(
            http("HTTP Request completedtasksreport skill")
              .get("/rest/v2/completedtasksreport/")
              .queryParam("dateFrom", "${data}")
              .queryParam("excludeNoAnswer", "false")
              .queryParam("orderBy", "ResultDate")
              .queryParam("orderDesc", "true")
              .queryParam("skip", "0")
              .queryParam("take",_.attributes.getOrElse("ten",None))
              .queryParam("skillIds", "${idSkill}")
              .check(status is 200)
          )
          .exec(
            http("HTTP Request completedtasksreport result")
              .get("/rest/v2/completedtasksreport/")
              .queryParam("dateFrom", "${data}")
              .queryParam("excludeNoAnswer", "false")
              .queryParam("orderBy", "ResultDate")
              .queryParam("orderDesc", "true")
              .queryParam("skip", "0")
              .queryParam("take", _.attributes.getOrElse("idSkill",None))
              .queryParam("resultId", "${idResult}")
              .check(status is 200)
          )
          .exec(
            http("HTTP Request completedtasksreport skill and result")
              .get("/rest/v2/completedtasksreport/")
              .queryParam("dateFrom", "${data}")
              .queryParam("excludeNoAnswer", "false")
              .queryParam("orderBy", "ResultDate")
              .queryParam("orderDesc", "true")
              .queryParam("skip", "0")
              .queryParam("take", _.attributes.getOrElse("idSkill",None))
              .queryParam("skillIds", "${idSkill}")
              .queryParam("resultId", "${idResult}")
              .check(status is 200)
          )
      }
}

