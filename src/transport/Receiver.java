package transport;

/**
 *<h1>Receiver</h1>
 * 135124
 * This is the receiver class it checks the packet and sends an acknowledgement if 
 * the packet received is correct else it doesn't send it.
 * 
 */
public class Receiver extends NetworkHost {
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
    
    // Add any necessary class variables here. They can hold state information for the receiver.
    // Also add any necessary methods (e.g. checksum of a String)

    /**
     * This creates a new packet 
     */
    private Packet pack;

    /**
     *this sets to the oldPacket 
     */
    private Packet oldPack; 

    /**
     *this sets the Ack Number
     */
    private int ackNo;

    /**
     *This sets the seq number 
     */
    private int seq;

    /**
     * This is the pack acknowledgement number 
     */
    private int packAckNo;

    /**
     *This is the packSequence number 
     */
    private int packSeq;

    /**
     *this is the check sum being created from the packets message, seq and Ack 
     */
    private int checkSum;

    /**
     *this is the checksum from the packets checksum field 
     */
    private int oldCheckSum;

   

    
    
    // This is the constructor.  Don't touch!

    /**
     *
     * @param entityName
     */
    public Receiver(int entityName) {
        super(entityName);
    }

   
    /**
     *<h1>init</h1> 
     * 
     * This method will be called once, before any of your other receiver-side methods are called. 
     * It can be used to do any required initialisation (e.g. of member variables you add to control the state of the receiver)
     */
    @Override
     public void init() {
        //Sets all of the values to their repsective values
         ackNo = 0;
         seq= 0;
         packAckNo = 0;
         packSeq = 0;
         checkSum = 0;
         oldCheckSum =0;
     }

  

    /**
     * <h1>input</h1> 
     * This method will be called whenever a packet sent from the sender(i.e. as a result of a udtSend() being called by the Sender ) arrives at the receiver. 
     * The argument "packet" is the (possibly corrupted) packet sent from the sender.
     * @param packet
     */
     @Override
     public void input(Packet packet) {
         pack = packet;
         if(checkOld(packet) == false){
            packAckNo = pack.getAcknum();
            packSeq = pack.getSeqnum();
            oldCheckSum = pack.getChecksum();
            checkSum = createCheckSum(pack.getPayload(), packSeq, packAckNo);
            if(checkAck() == true){
               oldPack = pack;
               deliverData(pack.getPayload());
               udtSend(new Packet(packSeq ,packAckNo, createCheckSum("", packSeq, packAckNo)));
            }
         }
         
     }
    

    /**
     *<h1>createCheckSum</h1> 
     * this creates a check sum from the message, sequence number and acknowledgement number 
     * @param message this is a string 
     * @param seq this is a int  
     * @param ackNo this is a int 
     * @return the checksum value 
     */
     public int createCheckSum(String message, int seq, int ackNo){
         int sum = 0;
         
         for( int i = 0; i < message.length(); i++){
               sum += message.charAt(i);
         }
         sum += seq + ackNo;
         return sum;
       
     }
       

    /**
     *<h1>checkAck</h1> 
     * check the packet that has come from the sender 
     * @return boolean true if it is correct 
     */
     public boolean checkAck(){
         if(oldCheckSum != checkSum){
            return false;
         }
         else if(packAckNo != ackNo){
            return false; 
         }
         else if(packSeq != seq){
             return false;
         }else{
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
            
             return true;
         }
         
         
     }
     

    /**
     *<h1>checkOld</h1>
     * 
     * This checks if the same packet has been sent before
     * @param packet this is the packet sent from the sender 
     * @return true if it is an old packet 
     */
     public boolean checkOld(Packet packet){
         if(oldPack != null){
            if(oldPack.getChecksum() == packet.getChecksum()){
               if(oldPack.getAcknum() == packet.getAcknum()){
                   if(oldPack.getSeqnum() == packet.getSeqnum()){
                        udtSend(new Packet(oldPack.getSeqnum() ,oldPack.getAcknum(), createCheckSum("",oldPack.getSeqnum(), oldPack.getAcknum())));
                        return true;
                   }else{

                       return false;
                   }

               }else{

                   return false; 
               }
           }else{
               return false;
           }
         }else
             {
                 return false;
             }
     } 
}
