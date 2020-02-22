package cj.netos.network;

import cj.netos.network.peer.IPeer;
import cj.studio.ecm.IServiceProvider;
import org.apache.commons.cli.CommandLine;

import java.util.HashMap;
import java.util.Map;


public final class CmdLine {
	IPeer peer;
	String cmd;
	CommandLine line;
	Map<String,Object> props;
	public CmdLine(String cmd, CommandLine line, IPeer peer) {
		this.cmd=cmd;
		this.line=line;
		props=new HashMap<>();
		this.peer=peer;
	}

	public IPeer peer() {
		return peer;
	}

	public String cmd() {
		return cmd;
	}
	public CommandLine line() {
		return line;
	}
	public String propString(String key){
		return (String)props.get(key);
	}
	public Object prop(String key){
		return props.get(key);
	}
	public void prop(String key,Object v){
		props.put(key, v);
	}
	public void copyPropsFrom(CmdLine cl) {
		props.putAll(cl.props);
	}
}
