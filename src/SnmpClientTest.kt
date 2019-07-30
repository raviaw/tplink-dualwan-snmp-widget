import org.snmp4j.CommunityTarget
import org.snmp4j.PDU
import org.snmp4j.Snmp
import org.snmp4j.event.ResponseEvent
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.Address
import org.snmp4j.smi.GenericAddress
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.VariableBinding
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.DefaultPDUFactory
import org.snmp4j.util.TreeUtils
import java.io.IOException
import java.util.TreeMap
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties

val use64 = true
val use32 = !use64
val prefix = if (use64) "1.3.6.1.2.1.31.1.1.1" else "1.3.6.1.2.1.2.2.1"
val databaseString = if (use64) """${prefix}.1 ifName
  ${prefix}.2 ifInMulticastPkts
  ${prefix}.3 ifInBroadcastPkts
  ${prefix}.4 ifOutMulticastPkts
  ${prefix}.5 ifOutBroadcastPkts
  ${prefix}.6 ifHCInOctets
  ${prefix}.7 ifHCInUcastPkts
  ${prefix}.8 ifHCInMulticastPkts
  ${prefix}.9 ifHCInBroadcastPkts
  ${prefix}.10 ifHCOutOctets
  ${prefix}.11 ifHCOutUcastPkts
  ${prefix}.12 ifHCOutMulticastPkts
  ${prefix}.13 ifHCOutBroadcastPkts
  ${prefix}.14 ifLinkUpDownTrapEnable
  ${prefix}.15 ifHighSpeed
  ${prefix}.16 ifPromiscuousMode
  ${prefix}.17 ifConnectorPresent
  ${prefix}.18 ifAlias
  ${prefix}.19 ifCounterDiscontinuityTime
  """
else """${prefix}.1 (ifIndex)
  ${prefix}.2 (ifDescr)
  ${prefix}.3 (ifType)
  ${prefix}.4 (ifMtu)
  ${prefix}.5 (ifSpeed)
  ${prefix}.6 (ifPhysAddress)
  ${prefix}.7 (ifAdminStatus)
  ${prefix}.8 (ifOperStatus)
  ${prefix}.9 (ifLastChange)
  ${prefix}.10 (ifInOctets)
  ${prefix}.11 (ifInUcastPkts)
  ${prefix}.12 (ifInNUcastPkts)
  ${prefix}.13 (ifInDiscards)
  ${prefix}.14 (ifInErrors)
  ${prefix}.15 (ifInUnknownProtos)
  ${prefix}.16 (ifOutOctets)
  ${prefix}.17 (ifOutUcastPkts)
  ${prefix}.18 (ifOutNUcastPkts)
  ${prefix}.19 (ifOutDiscards)
  ${prefix}.20 (ifOutErrors)
  ${prefix}.21 (ifOutQLen)
  ${prefix}.22 (ifSpecific)
  """

val database =
  databaseString.split("\n")
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .map {
      it.substringBefore(" ") to it.substringAfter(" ")
        .removePrefix("(")
        .removeSuffix(")")
    }
    .toMap(linkedMapOf())

val reverseDatabase = database.map { it -> it.value to it.key }.toMap()

var linkWan1: Int? = null
var linkWan2: Int? = null
var wan1InDelta: Long? = null
var wan2InDelta: Long? = null
var wan1OutDelta: Long? = null
var wan2OutDelta: Long? = null

lateinit var form: Brublebees

/**
 * @author [Ravi Wallau](mailto:raviw@emerald-associates.com)
 */
fun main(args: Array<String>) {
  val target = CommunityTarget<Address>()
  target.setCommunity(OctetString("public"))
  target.setAddress(GenericAddress.parse("udp:192.168.10.1/161")) // supply your own IP and port
  target.setRetries(2)
  target.setTimeout(1500)
  target.setVersion(SnmpConstants.version2c)

  form = Brublebees()
  form.isVisible = true

  thread(
    isDaemon = true,
    block = { updateVariables(target) }
  )

/*
  for (entry in result.entries) {
    val key = entry.key.removePrefix(".")
    val mapKey = key.substringBeforeLast(".")
    val subItem = key.substringAfterLast(".")
    println(mapKey + ":" + database[mapKey])
    println("${entry.value.variableBinding.oid}/${entry.value.variableBinding.variable}")
    if (entry.key.startsWith(".${prefix}.2.")) {
      println("ifDescr" + entry.key.replace(".${prefix}.2", "") + ": " + entry.value.variableBinding)
    }
    if (entry.key.startsWith(".${prefix}.3.")) {
      println("ifType" + entry.key.replace(".${prefix}.3", "") + ": " + entry.value.variableBinding)
    }
*/
/*

    println(getAsString(snmp, entry.value.oid))
    println()
    println()
    println()
  }
*/
}

private fun updateVariables(target: CommunityTarget<Address>) {
  var wan1InLast: Long? = null
  var wan2InLast: Long? = null
  var wan1OutLast: Long? = null
  var wan2OutLast: Long? = null
  while (true) {
    val result = doWalk(".${prefix}", target) // ifTable, mib-2 interfaces

    //val index = checkNotNull(reverseDatabase["ifIndex"]?:reverseDatabase["ifName"]) { "ifIndex" } + "."
    val indexNumbers = result.keys.map { it.substringAfterLast(".") }.toSet().toList().sortedBy { it.toInt() }
    val indexes = indexNumbers.map { Parameters(it) }
    //val indexes = result.filter { it.key.startsWith(index) }.map { Parameters(it.value.variableBinding.toValueString()) }
    val parameterClass = Parameters::class
    for (entry in database) {
      val key = entry.value
      val property = parameterClass.declaredMemberProperties.filter { it.name == key }.first() as KMutableProperty<String>
      for (index in indexes) {
        val readValueKey = entry.key + "." + index.ifIndex
        val value = result[readValueKey]?.variableBinding?.toValueString()
        property.setter.call(index, value)
      }
    }
    val indexMap = indexes.associateBy { it.ifName }
    val wan1 = indexMap["pppoe-wan1_poe"]
    val wan2 = indexMap["pppoe-wan2_poe"]
    val wan1InCurrent = wan1?.ifHCInOctets?.toLong()
    val wan2InCurrent = wan2?.ifHCInOctets?.toLong()
    val wan1OutCurrent = wan1?.ifHCOutOctets?.toLong()
    val wan2OutCurrent = wan2?.ifHCOutOctets?.toLong()

    linkWan1 = wan1?.ifConnectorPresent?.toInt()
    linkWan2 = wan2?.ifConnectorPresent?.toInt()

    if (wan1InCurrent != null) wan1InDelta = delta(wan1InCurrent, wan1InLast)
    if (wan2InCurrent != null) wan2InDelta = delta(wan2InCurrent, wan2InLast)
    if (wan1OutCurrent != null) wan1OutDelta = delta(wan1OutCurrent, wan1OutLast)
    if (wan2OutCurrent != null) wan2OutDelta = delta(wan2OutCurrent, wan2OutLast)

    println("wan1.link: ${linkWan1}")
    println("wan2.link: ${linkWan2}")
    println("wan1.in: ${wan1InDelta}")
    println("wan2.in: ${wan2InDelta}")
    println("wan1.out: ${wan1OutDelta}")
    println("wan2.out: ${wan2OutDelta}")

    SwingUtilities.invokeLater { form.updateControls(10000) }

    Thread.sleep(10000)

    wan1InLast = wan1InCurrent ?: wan1InLast
    wan2InLast = wan2InCurrent ?: wan2InLast
    wan1OutLast = wan1OutCurrent ?: wan1OutLast
    wan2OutLast = wan2OutCurrent ?: wan2OutLast
  }
}

fun delta(current: Long?, last: Long?): Long {
  if (current == null || last == null) {
    return 0
  }
  return current - last
}

@Throws(IOException::class)
fun getAsString(snmp: Snmp, oid: OID): String {
  val event = get(snmp, arrayOf(oid))
  return event.getResponse().get(0).getVariable().toString()
}

@Throws(IOException::class)
fun get(snmp: Snmp, oids: Array<OID>): ResponseEvent<*> {
  val pdu = PDU()
  for (oid in oids) {
    pdu.add(VariableBinding(oid))
  }
  pdu.type = PDU.GET
  val event = snmp.send(pdu, getTarget(), null)
  if (event != null) {
    return event
  }
  throw RuntimeException("GET timed out")
}

@Throws(IOException::class)
fun doWalk(tableOid: String, target: org.snmp4j.Target<Address>): Map<String, OidEntry> {
  val result = TreeMap<String, OidEntry>()
  val transport = DefaultUdpTransportMapping()
  val snmp = Snmp(transport)
  transport.listen()

  val treeUtils = TreeUtils(snmp, DefaultPDUFactory())
  val events = treeUtils.getSubtree(target, OID(tableOid))
  if (events == null || events.size == 0) {
    println("Error: Unable to read table...")
    return result
  }

  for (event in events) {
    if (event == null) {
      continue
    }
    if (event.isError) {
      println("Error: table OID [" + tableOid + "] " + event.errorMessage)
      continue
    }

    val varBindings = event.variableBindings
    if (varBindings == null || varBindings.size == 0) {
      continue
    }
    for (varBinding in varBindings) {
      if (varBinding == null) {
        continue
      }

      result[varBinding.oid.toString()] = OidEntry(varBinding, varBinding.oid, varBinding.variable.toString())
    }
  }
  snmp.close()

  return result
}

private val address = "192.168.10.1"

private fun getTarget(): CommunityTarget<Address> {
  val targetAddress = GenericAddress.parse("udp:192.168.10.1/161")
  val target = CommunityTarget<Address>()
  target.setCommunity(OctetString("public"))
  target.setAddress(targetAddress)
  target.setRetries(2)
  target.setTimeout(1500)
  target.setVersion(SnmpConstants.version2c)
  return target
}

class OidEntry(val variableBinding: VariableBinding, val oid: OID, val string: String) {
  override fun toString(): String {
    return string
  }
}

class Parameters {
  constructor() {
  }

  constructor(ifIndex: String) {
    this.ifIndex = ifIndex
  }

  var ifIndex: String? = null
  var ifDescr: String? = null
  var ifType: String? = null
  var ifMtu: String? = null
  var ifSpeed: String? = null
  var ifPhysAddress: String? = null
  var ifAdminStatus: String? = null
  var ifOperStatus: String? = null
  var ifLastChange: String? = null
  var ifInOctets: String? = null
  var ifInUcastPkts: String? = null
  var ifInNUcastPkts: String? = null
  var ifInDiscards: String? = null
  var ifInErrors: String? = null
  var ifInUnknownProtos: String? = null
  var ifOutOctets: String? = null
  var ifOutUcastPkts: String? = null
  var ifOutNUcastPkts: String? = null
  var ifOutDiscards: String? = null
  var ifOutErrors: String? = null
  var ifOutQLen: String? = null
  var ifSpecific: String? = null
  var ifName: String? = null
  var ifInMulticastPkts: String? = null
  var ifInBroadcastPkts: String? = null
  var ifOutMulticastPkts: String? = null
  var ifOutBroadcastPkts: String? = null
  var ifHCInOctets: String? = null
  var ifHCInUcastPkts: String? = null
  var ifHCInMulticastPkts: String? = null
  var ifHCInBroadcastPkts: String? = null
  var ifHCOutOctets: String? = null
  var ifHCOutUcastPkts: String? = null
  var ifHCOutMulticastPkts: String? = null
  var ifHCOutBroadcastPkts: String? = null
  var ifLinkUpDownTrapEnable: String? = null
  var ifHighSpeed: String? = null
  var ifPromiscuousMode: String? = null
  var ifConnectorPresent: String? = null
  var ifAlias: String? = null
  var ifCounterDiscontinuityTime: String? = null
  override fun toString(): String {
    return "Parameters(ifIndex=$ifIndex,\n ifDescr=$ifDescr,\n ifType=$ifType,\n ifMtu=$ifMtu,\n ifSpeed=$ifSpeed,\n ifPhysAddress=$ifPhysAddress,\n ifAdminStatus=$ifAdminStatus,\n ifOperStatus=$ifOperStatus,\n ifLastChange=$ifLastChange,\n ifInOctets=$ifInOctets,\n ifInUcastPkts=$ifInUcastPkts,\n ifInNUcastPkts=$ifInNUcastPkts,\n ifInDiscards=$ifInDiscards,\n ifInErrors=$ifInErrors,\n ifInUnknownProtos=$ifInUnknownProtos,\n ifOutOctets=$ifOutOctets,\n ifOutUcastPkts=$ifOutUcastPkts,\n ifOutNUcastPkts=$ifOutNUcastPkts,\n ifOutDiscards=$ifOutDiscards,\n ifOutErrors=$ifOutErrors,\n ifOutQLen=$ifOutQLen,\n ifSpecific=$ifSpecific,\n ifName=$ifName,\n ifInMulticastPkts=$ifInMulticastPkts,\n ifInBroadcastPkts=$ifInBroadcastPkts,\n ifOutMulticastPkts=$ifOutMulticastPkts,\n ifOutBroadcastPkts=$ifOutBroadcastPkts,\n ifHCInOctets=$ifHCInOctets,\n ifHCInUcastPkts=$ifHCInUcastPkts,\n ifHCInMulticastPkts=$ifHCInMulticastPkts,\n ifHCInBroadcastPkts=$ifHCInBroadcastPkts,\n ifHCOutOctets=$ifHCOutOctets,\n ifHCOutUcastPkts=$ifHCOutUcastPkts,\n ifHCOutMulticastPkts=$ifHCOutMulticastPkts,\n ifHCOutBroadcastPkts=$ifHCOutBroadcastPkts,\n ifLinkUpDownTrapEnable=$ifLinkUpDownTrapEnable,\n ifHighSpeed=$ifHighSpeed,\n ifPromiscuousMode=$ifPromiscuousMode,\n ifConnectorPresent=$ifConnectorPresent,\n ifAlias=$ifAlias,\n ifCounterDiscontinuityTime=$ifCounterDiscontinuityTime)"
  }
}