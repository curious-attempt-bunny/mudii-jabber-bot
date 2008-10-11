package com.albery.jabberbot
import org.jivesoftware.smack.*

class Main {
	static void main(args) {
		if (args.length == 3)
			new Main(user:args[0], password:args[1], partner:args[2]).run()
		else
			println "Usage: <user> <password> <partner jabber id>"
	}
	
	def user
	def password
	def partner
	
	def run() {
		XMPPConnection connection = new XMPPConnection("gmail.com")
		
		connection.connect()
		connection.login("$user@gmail.com", password)
		
		Roster roster = connection.getRoster()
		roster.addRosterListener([
            presenceChanged:{presence ->
		    	println "$presence.from available: $presence.available"
		    }
		] as RosterListener)

        roster.reload()

		def chat = connection.getChatManager().createChat(partner, { chat, message ->
	        println("Received message: " + message.body)
		} as MessageListener)

		chat.sendMessage("Hi!")

		Thread.sleep(5000)
	}
}