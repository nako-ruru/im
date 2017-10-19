/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.im.compute.adapter.service;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.mycompany.im.compute.domain.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

/**
 *
 * @author Administrator
 */
@Service
public class RedisServiceRegistry implements ServiceRegistry, ApplicationListener<ApplicationEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Resource(name = "plain.tcp.listen.port")
    private int listenPort;
    @Resource(name = "registryRedisTemplate")
    private StringRedisTemplate redisTemplate;
    
    private String registryAddress;

    @Override
    public void register() {
        registerIfRigstryAddressResolved();
    }

    private void registerIfRigstryAddressResolved() {
        if(registryAddress != null) {
            Map map = ImmutableMap.of("registerTime", System.currentTimeMillis());
            final String value = new Gson().toJson(map);
            Logger timerLogger = LoggerFactory.getLogger("timer");
            timerLogger.info("register: key: " + registryAddress + "; value: " + value);
            redisTemplate.opsForHash().put("compute-servers", registryAddress, value);
        }
    }
    
    private String resolveAddress() {
        return resolveIp() + ":" + listenPort;
    }
    
    private String resolveIp() {
        return new NetworkInterfaceIpResolver().resolveIp();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        registryAddress = resolveAddress();
        logger.info("resolve ip: " + registryAddress);
    }

    private interface IpResolver {
        String resolveIp();
    }
    
    private static class NetworkInterfaceIpResolver implements IpResolver {

        @Override
        public String resolveIp() {
            try {
                return getLocalHostLANAddress().getHostAddress();
            } catch (UnknownHostException ex) {
                throw new Error(ex);
            }
        }
    
        /**
         * Returns an <code>InetAddress</code> object encapsulating what is most
         * likely the machine's LAN IP address.
         * <p/>
         * This method is intended for use as a replacement of JDK method
         * <code>InetAddress.getLocalHost</code>, because that method is ambiguous
         * on Linux systems. Linux systems enumerate the loopback network interface
         * the same way as regular LAN network interfaces, but the JDK
         * <code>InetAddress.getLocalHost</code> method does not specify the
         * algorithm used to select the address returned under such circumstances,
         * and will often return the loopback address, which is not valid for
         * network communication. Details
         * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
         * <p/>
         * This method will scan all IP addresses on all network interfaces on the
         * host machine to determine the IP address most likely to be the machine's
         * LAN address. If the machine has multiple IP addresses, this method will
         * prefer a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually
         * IPv4) if the machine has one (and will return the first site-local
         * address if the machine has more than one), but if the machine does not
         * hold a site-local address, this method will return simply the first
         * non-loopback address found (IPv4 or IPv6).
         * <p/>
         * If this method cannot find a non-loopback address using this selection
         * algorithm, it will fall back to calling and returning the result of JDK
         * method <code>InetAddress.getLocalHost</code>.
         * <p/>
         *
         * @throws UnknownHostException If the LAN address of the machine cannot be
         * found.
         */
        private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
            try {
                InetAddress candidateAddress = null;
                //Iterate all NICs (network interface cards)...
                for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                    NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                    //Iterate all IP addresses assigned to each card...
                    for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                        InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                        if (!inetAddr.isLoopbackAddress()) {

                            if (inetAddr.isSiteLocalAddress()) {
                                //Found non-loopback site-local address. Return it immediately...
                                return inetAddr;
                            } else if (candidateAddress == null) {
                                //Found non-loopback address, but not necessarily site-local.
                                //Store it as a candidate to be returned if site-local address is not subsequently found...
                                candidateAddress = inetAddr;
                            //Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            //only the first. For subsequent iterations, candidate will be non-null.
                            }
                        }
                    }
                }
                if (candidateAddress != null) {
                    //We did not find a site-local address, but we found some other non-loopback address.
                    //Server might have a non-site-local address assigned to its NIC (or it might be running
                    //IPv6 which deprecates the"site-local" concept).
                    //Return this non-loopback candidate address...
                    return candidateAddress;
                }
                //At this point, we did not find a non-loopback address.
                //Fall back to returning whatever InetAddress.getLocalHost() returns...
                InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
                if (jdkSuppliedAddress == null) {
                    throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
                }
                return jdkSuppliedAddress;
            } catch (Exception e) {
                UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address:" + e);
                unknownHostException.initCause(e);
                throw unknownHostException;
            }
        }
        
    }
    
}
