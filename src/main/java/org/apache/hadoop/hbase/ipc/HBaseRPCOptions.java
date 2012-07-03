package org.apache.hadoop.hbase.ipc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.hbase.io.hfile.Compression;
import org.apache.hadoop.hbase.ipc.HBaseRPC.VersionMismatch;
import org.apache.hadoop.io.Writable;

public class HBaseRPCOptions implements Writable {
  
  public static final HBaseRPCOptions DEFAULT = new HBaseRPCOptions ();
  
  private static final byte VERSION_INITIAL = 1;
	
  private byte version = VERSION_INITIAL;
  private Compression.Algorithm compressionAlgo = Compression.Algorithm.NONE;
	private boolean requestProfiling = false;
	private String tag = null;
	
	// this will be used as profiling data in htable so it's possible to
	// set it after receiving profiling data. do not need to serialize this.
	public ProfilingData profilingResult = new ProfilingData ();
	
	public HBaseRPCOptions () {}
	
	public void setVersion (byte version) {
	  this.version = version;
	}
	
	public byte getVersion () {
    return this.version;
  }
	
	public void setRPCCompression(Compression.Algorithm compressionAlgo) {
    this.compressionAlgo = compressionAlgo;
  }

  public Compression.Algorithm getRPCCompression() {
    return this.compressionAlgo;
  }
	
  /**
   * set whether to request profiling data form the server
   *
   * @param request request profiling or not
   */
	public void setRequestProfiling (boolean request) {
    this.requestProfiling = request;
  }
  
  public boolean getRequestProfiling () {
    return this.requestProfiling;
  }
  
  /**
   * set the tag of this rpc call. The server will aggregate stats
   * based on the tag of the rpc call. typically this is unique to the
   * application making the call
   *
   * @param tag RPC tag of the call
   */
  public void setTag (String tag) {
    this.tag = tag;
  }
  
  public String getTag () {
    return this.tag;
  }
	
	@Override
  public void write(DataOutput out) throws IOException {
	  // 1. write the object version
	  out.writeByte(this.version);
	  
	  // 2. write the compression algo used to compress the request being sent
    out.writeUTF(this.compressionAlgo.getName());
    
    // 3. write the compression algo to use for the response
    out.writeUTF(this.compressionAlgo.getName());
    
    // 4. write profiling request flag
	  out.writeBoolean(this.requestProfiling);
	  
	  // 5. write tag flag and tag if flag is true
	  out.writeBoolean(this.tag != null ? true : false);
	  if (this.tag != null) {
	    out.writeUTF(this.tag);
	  }
  }
    
  @Override
  public void readFields(DataInput in) throws IOException {
    this.version = in.readByte ();
    if (this.version > VERSION_INITIAL) {
      // this version is not handled!
      throw new VersionMismatch("HBaseRPCOptions", this.version,
          VERSION_INITIAL);
    }
    String compressionName;
    compressionName = in.readUTF ();
    compressionName = in.readUTF ();          // dummy read
    this.compressionAlgo = Compression.
        getCompressionAlgorithmByName(compressionName);
    this.requestProfiling = in.readBoolean();
    this.tag = null;
    if (in.readBoolean()) {
      this.tag = in.readUTF ();
    }
  }
}
