package basetime.model

final case class Command(
  topic : String,       // one of the model classes (Consumer, Producer, Person, Contract, Hour)
  method: String,       // create, update, delete
  data  : String        // JSON containing the data of an instance of topic
)

// Command is received via HTTP POST, PUT or DELETE (translates to method)
// Each command is persisted for recovery later on

object Command {
    val topics = List("consumer", "worker", "approver", "producer")
}