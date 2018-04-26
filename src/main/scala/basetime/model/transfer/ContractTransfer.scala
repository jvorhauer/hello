package basetime.model.transfer

import java.time.ZonedDateTime
import java.util.UUID


final case class ContractTransfer(
  uuid    : UUID,
  start   : ZonedDateTime,
  end     : ZonedDateTime,
  consumer: UUID,
  worker  : String      // actually an email address
)
