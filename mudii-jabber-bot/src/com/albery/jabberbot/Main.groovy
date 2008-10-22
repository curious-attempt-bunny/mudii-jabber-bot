package com.albery.jabberbot
import org.jivesoftware.smack.*
import com.albery.comms.*

class Main {
	static void main(args) {
		if (args.length == 6)
			new Main(user:args[0], password:args[1], partner:args[2], mudAccount: args[3], mudPassword: args[4], mudUser: args[5] ).run()
		else
			println "Usage: <user> <password> <partner jabber id> <mud account> <mud password> <mud user>"
	}
	
	def user
	def password
	def partner
	def mudAccount, mudPassword, mudUser
	
	def run() {
		XMPPConnection connection = new XMPPConnection("gmail.com")
		
		connection.connect()
		connection.login("$user@gmail.com", password)
		
//		Roster roster = connection.getRoster()
//		roster.addRosterListener([
//            presenceChanged:{presence ->
//		    	println "$presence.from available: $presence.available"
//		    }
//		] as RosterListener)
//
//        roster.reload()
//

		def mudii = new MudIIConnection(mudAccount:mudAccount, mudPassword:mudPassword, mudUser:mudUser)
		def echoNext = false
		
		def chat = connection.getChatManager().createChat(partner, { chat, message ->
	        try {
	        	mudii.sendCommand(message.body)
	        	echoNext = true
	        } catch (RuntimeException e) {
	        	chat.sendMessage(e.getMessage())
	        }
		} as MessageListener)

		while(true) {
			mudii.eachChunk { chunk ->
				println "CHUNK: $chunk"
				if (chunk.startsWith("<09") || echoNext == true || chunk.startsWith("<05")) {
					echoNext = false
					def event = chunk.replaceAll("<01><0102>\\*<><>", "").replaceAll("<.*?>", "").trim()
					println "EVENT: "+event
					chat.sendMessage(event)
				}
			}
			chat.sendMessage("Reset");
		}
	}
}