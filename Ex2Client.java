// Luis Cortes
// CS 380
// Exercise 2
//

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.*;
import java.util.zip.CRC32;

public class Ex2Client {
	private static byte[] sequence; // sequence of bytes
	private static byte[] checkSum; // For checksum
	private static int index = 0;
	private static int checkSumIndex = 0;

	public static void main(String[] args) {

		try  {
			// Get IP Address 
			InetAddress address = InetAddress.getByName(
				new URL("http://codebank.xyz").getHost());
			String ip = address.getHostAddress();


			Socket socket = new Socket(ip, 38102);
			System.out.println("Connected to server");

			InputStream is = socket.getInputStream();
			PrintStream outStream = new PrintStream(socket.getOutputStream(), true);
			sequence = new byte[100]; // Total number of bytes to be used for message


			System.out.println("Received bytes: ");
			System.out.print("   ");
			int columnCount = 0; // To keep track of when to put newline 
			for (int i = 0; i < 100; i++) { // Every iteration 'read' twice: 200 total reads

				// Get first 4-bits
				byte firstBits = (byte)is.read();
				System.out.print(Integer.toHexString(firstBits & 0xF));
				columnCount++;

				firstBits = (byte)(firstBits << 4); // Move to upper four bits of byte

				// Get second 4-bits
				byte secondBits = (byte)is.read();
				System.out.print(Integer.toHexString(secondBits & 0xF));
				columnCount++;

				// Start a new line
				if (columnCount % 20 == 0) {
					System.out.print("\n   ");
				}

				byte byteSeq =  (byte)(firstBits | secondBits); // Combine to make a byte 
				sequence[index++] = byteSeq; // Add byte to sequence
			
			}

			// Create the error code
			CRC32 check = new CRC32();
			check.update(sequence,0, sequence.length); 

			// Display the error code
			System.out.println("\nGenerated CRC32: "+Long.toHexString(check.getValue()));

			checkSum = new byte[4]; // Array of 4 bytes

			// Screen a byte from the Long from left to right
			for (int i=24; i >= 0; i-=8) {
				Long shifted = check.getValue() >> i; // Gets desired byte
				byte bytePattern = (byte) (shifted & 0xFF); // Get byte value
				// System.out.println(Integer.toHexString(bytePattern & 0xFF));

				checkSum[checkSumIndex++] = bytePattern;
			}

			outStream.write(checkSum, 0, checkSum.length); // Send to server

			byte response = (byte) is.read(); // Receive response

			// Check response
			if (response == 1) { 
				System.out.println("Response Good.");
			} else {
				System.out.println("RESPONSE BAD");
			}

		} catch (Exception e) {e.printStackTrace();}
	} // end main


}