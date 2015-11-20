/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework2;

import homework2.bank.Bank;
import homework2.bank.BankImpl;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 *
 * @author David
 */
public class MarketServer {
    
    private static final String USAGE = "java bankrmi.Server <bank_rmi_url>";
    private static final String MARKET = "MyMarket";

    public MarketServer(String bankName) {
        try {
            MarketRequest bankobj = new MarketRequestImpl();
            // Register the newly created object at rmiregistry.
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            Naming.rebind(bankName, bankobj);
            System.out.println(bankobj + " is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        if (args.length > 1 || (args.length > 0 && args[0].equalsIgnoreCase("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }
        new MarketServer(MARKET);
    }
    
}
