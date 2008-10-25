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
		
		def online = false
		
		Roster roster = connection.getRoster()
		roster.addRosterListener([
            presenceChanged:{presence ->
		    	println "$presence.from away: $presence.away"
		    	if (presence.from.contains(partner)) {
		    		online = !presence.away
		    	}
		    }
		] as RosterListener)

        roster.reload()

        def mudii = new MudIIConnection(mudAccount:mudAccount, mudPassword:mudPassword, mudUser:mudUser)
		def echoNext = false
		
		def chat = connection.getChatManager().createChat(partner, { chat, message ->
	        try {
	        	if (message.body) {
	        		if (mudii.output) {
			        	mudii.sendCommand(message.body)
			        	echoNext = message.body
	        		}
	        	}
	        } catch (RuntimeException e) {
	        	chat.sendMessage(e.getMessage())
	        }
		} as MessageListener)

		while(true) {
			while (!online) {
				println "[away]"
				Thread.sleep(5000)
			}
			
			try {
				mudii.eachChunk { chunk ->
					if (!online) throw new InterruptedException()
					
					println "CHUNK: $chunk"
					if (chunk.startsWith("<09") || echoNext || chunk.startsWith("<05")) {
						def event = chunk.replaceAll("<01><0102>\\*<><>", "").replaceAll("<.*?>", "").trim()
						if (echoNext && event.startsWith(echoNext)) {
	                        event = event.substring(echoNext.length()).trim()
	                    }
						println "EVENT: "+event
						chat.sendMessage(event)
						echoNext = false
					}
				}
				chat.sendMessage("Reset");
			} catch (InterruptedException e) {
				println "[away]"
			}
		}
	}
}