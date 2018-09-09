package transport;

/**<h1>Sender</h1>
 * 135124
 * This is the Sender class, this generates a packet from the message using 
 * an sequence number, acknowledgement number and the char int of the message 
 * It then sends the packet to the receiver, it then receives an acknowledgement
 * from the receiver.
 * 
 * If the acknowledgement is correct it will send the next packet, however, if it
 * is incorrect it will cause a timer interrupt which will in turn then send the
 * old packet. 
 */
public class Sender extends NetworkHost {

    /*
     * Predefined Constant (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in "increment" time units, causing the interrupt handler to be called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to app layer. You should only call this in the Receiver class.
     *
     *  Predefined Classes:
     *
     *  NetworkSimulator: Implements the core functionality of the simulator
     *
     *  double getTime()
     *       Returns the current time in the simulator. Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().getTime()
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().printEventList()
     *
     *  Message: Used to encapsulate a message coming from the application layer
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      void setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *      String getData():
     *          returns the data contained in the message
     *
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload):
     *          creates a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and a payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and an empty payload
     *    Methods:
     *      void setSeqnum(int seqnum)
     *          sets the Packet's sequence field to seqnum
     *      void setAcknum(int acknum)
     *          sets the Packet's ack field to acknum
     *      void setChecksum(int checksum)
     *          sets the Packet's checksum to checksum
     *      void setPayload(String payload) 
     *          sets the Packet's payload to payload
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */
    
    // Add any necessary class variables here. They can hold state information for the sender. 
    // Also add any necessary methods (e.g. checksum of a String)
    
    
    // This is the constructor.  Don't touch!

    /**
     *
     * @param entityName
     */
    public Sender(int entityName) {
        super(entityName);
    }

    

    /**
     * Used to set the sequence number to the packet 
     */
    private int seq;

    /**
     *Used to set the acknowledgement time to the packet 
     */
    private int ackNo;

    /**
     *used to set the delay of the packet 
     */
    private double delay;

    /**
     * used to set the checksum of the packet 
     */
    private int checkSum;

    /**
     *Used to get a new message 
     */
    private Message mes;

    /**
     *used to check if a packet has been retrieved 
     */
    private boolean retrieved;
    
    /**
     * used to create a new packet 
     */
    private Packet pack = null;

    /**
     * this is used for the Round trip time of the packet 
     */
    private  double RTT;

    /**
     *this is used for the increment value of the start timer
     */
    private double incr;

    /**
     * <h1>init</h1> 
     * This method will be called once, before any of your other sender-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the sender).
     */
    @Override
    
     public void init() {
         //This inisialises the values; 
         seq = 0;
         ackNo = 0;
         delay = 10;
         checkSum = 0;
         retrieved = true;
         RTT = delay * 2;
         incr = RTT * 2;
         
     }
    
     
    /**
     *<h1>output</h1> 
     * 
     * This method will be called whenever the app layer at the sender has a message to send.  
     * The job of your protocol is to ensure that the data in such a message is delivered in-order, and correctly, to the receiving application layer.
     * @param message
     */
     @Override
     public void output(Message message) {
       //if previous message is retrieved then send the new message, else resend the message 
            if(retrieved == true){
                mes = message;
                int checkSum = createCheckSum(mes);
                pack = new Packet(seq,ackNo,checkSum, mes.getData());
                startTimer(incr);
                udtSend(pack);
                retrieved  = false; 
            }else{
                stopTimer();
                startTimer(incr);
                udtSend(pack);
            }
     }
    
    
    

    /**
     * This method will be called whenever a packet sent from the receiver (i.e. as a result of a udtSend() being done by a receiver procedure) arrives at the sender.  
     * "packet" is the (possibly corrupted) packet sent from the receiver.
     * 
     * @param packet
     */
    @Override
    public void input(Packet packet) {
        checkAck(packet);
     }
    
    
    

    /**
     * This method will be called when the senders's timer expires (thus generating a timer interrupt). 
     * You'll probably want to use this method to control the retransmission of packets. 
     * See startTimer() and stopTimer(), above, for how the timer is started and stopped. 
     */
    @Override
     public void timerInterrupt() {
         startTimer(incr);
         udtSend(pack);
         
     }
     
     

    /**
     * <h1>checkAck</h1> 
     * This method will be called in the output method and input method
     * to help check the checksum of sending of packets and receiving of packets 
     * 
     * @param packet this is the packet to be checked 
     */
     public void checkAck(Packet packet){
        if(packet.getChecksum() != createCheckSum(packet.getPayload(), packet.getSeqnum(), packet.getAcknum())){
             output(mes);
         }
         else{
             if(seq == 0){
                seq = 1;
             }else{
                 seq = 0;
             }
             if(ackNo == 0){
                 ackNo = 1;
             }else{
                 ackNo = 0;
             }
             checkSum = 0;
             stopTimer();
             retrieved = true;
         }          
     }
     
     

    /**
     *<h1>createCheckSum</h1>
     * 
     * This method helps create a checksum for each message 
     * @param message this is a message which from the application layer 
     * @return
     */
     public int createCheckSum(Message message){
         int sum = 0;
         //Loops through the message 
         for( int i = 0; i < message.getData().length(); i++){
               sum += message.getData().charAt(i);
         }
         sum += seq + ackNo;
         return sum;
       
     }
     

    /**
     *This creates a checksum using a message, sequence number and ackNo
     * 
     * @param message string which gets the character value 
     * @param seq int which gets the value of the sequence number 
     * @param ackNo int which gets the value of the ackNo 
     * @return
     */
     public int createCheckSum(String message, int seq, int ackNo){
         int sum = 0;
         //loops through the message 
         for( int i = 0; i < message.length(); i++){
               sum += message.charAt(i);
         }
         sum += seq + ackNo;
         return sum;
     }
}
