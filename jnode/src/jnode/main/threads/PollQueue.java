package jnode.main.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jnode.dto.Link;
import jnode.logger.Logger;
import jnode.protocol.binkp.BinkpConnector;
import jnode.protocol.io.Connector;
import jnode.protocol.io.exception.ProtocolException;

/**
 * 
 * @author kreon
 * 
 */
public enum PollQueue {
	INSTANSE;
	private static Logger logger = Logger.getLogger(PollQueue.class);
	private Set<Link> queue;

	private PollQueue() {
		queue = new HashSet<Link>();
	}

	public void poll() {
		if (queue.size() > 0) {
			logger.l4("В PollQueue " + queue.size() + " узлов, делаем poll");
			ArrayList<Link> currentQueue = new ArrayList<Link>(queue);
			queue = new HashSet<Link>();
			for (Link link : currentQueue) {
				new Poll(link).start();
			}
		}
	}

	public void add(Link link) {
		queue.add(link);
	}

	public void addAll(Collection<Link> links) {
		queue.addAll(links);
	}

	private static class Poll extends Thread {
		private static final Logger logger = Logger.getLogger(Poll.class);
		private Link link;

		public Poll(Link link) {
			super();
			this.link = link;
		}

		@Override
		public void run() {
			try {
				BinkpConnector binkpConnector = new BinkpConnector();
				Connector connector = new Connector(binkpConnector);
				logger.l3(String.format("Соединяемся с %s (%s:%d)",
						link.getLinkAddress(), link.getProtocolHost(),
						link.getProtocolPort()));
				connector.connect(link);

			} catch (ProtocolException e) {
				logger.l2("Ошибка протокола:" + e.getMessage());
			}
		}
	}
}
