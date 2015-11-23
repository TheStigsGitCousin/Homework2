/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package Views;

import homework2.Item;
import homework2.MarketRequest;
import homework2.Owner;
import homework2.OwnerImpl;
import homework2.bank.Bank;
import homework2.bank.Account;
import homework2.bank.RejectedException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author David
 */
public class ClientPanel extends Panel {
    
    private static MarketRequest market;
    private static Bank bankobj;
    private static final String DEFAULT_BANK_NAME = "Nordea";
    private static Owner owner;
    
    // Game components
    private JLabel statusMessageLabel=new JLabel("");
    private JLabel currentGuessLabel=new JLabel("Start the game!");
    private JTextField accountNameTextField=new JTextField(10);
    private JButton registerButton=new JButton("Register/Log in");
    private JButton listItemsButton=new JButton("List items");
    // Connection components
    private JTextField itemNameTextField=new JTextField(10);
    private JTextField itemPriceTextField=new JTextField(10);
    private JButton sellButton=new JButton("sell");
    
    
    public ClientPanel(String bankName) throws RemoteException{
        
        try {
            LocateRegistry.getRegistry(1099).list();
        } catch (RemoteException e) {
            LocateRegistry.createRegistry(1099);
        }
        try {
            market=(MarketRequest)Naming.lookup("MarketRequest");
            bankobj = (Bank) Naming.lookup(bankName);
        } catch (NotBoundException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        setLayout(new BorderLayout());
        constructComponents();
    }
    
    public ClientPanel() throws RemoteException {
        this(DEFAULT_BANK_NAME);
    }
    
    private void constructComponents(){
        
        registerButton.addActionListener((ActionEvent e)->{ registerOrLogIn(); });
        accountNameTextField.addActionListener((ActionEvent e)->{ registerOrLogIn(); });
        
        itemNameTextField.addActionListener((ActionEvent e)->{ sell(); });
        sellButton.addActionListener((ActionEvent e)->{ sell(); });
        
        listItemsButton.addActionListener((ActionEvent e)->{ listItems();});
        JPanel gamePanel=new JPanel();
        gamePanel.setLayout(new BorderLayout());
        JPanel guessPanel=new JPanel();
        guessPanel.setLayout(new FlowLayout());
        guessPanel.add(new JLabel("account name"));
        guessPanel.add(accountNameTextField);
        guessPanel.add(registerButton);
        guessPanel.add(listItemsButton);
        gamePanel.add(guessPanel, BorderLayout.NORTH);
        gamePanel.add(currentGuessLabel, BorderLayout.CENTER);
        add(gamePanel, BorderLayout.NORTH);
        
        JPanel connectionPanel=new JPanel();
        connectionPanel.setLayout(new FlowLayout());
        connectionPanel.add(new JLabel("name"));
        connectionPanel.add(itemNameTextField);
        connectionPanel.add(new JLabel("price"));
        connectionPanel.add(itemPriceTextField);
        connectionPanel.add(sellButton);
        // Add connectionPanel to ClientPanel
        add(connectionPanel, BorderLayout.CENTER);
        
        add(statusMessageLabel, BorderLayout.SOUTH);
    }
    
    private void listItems(){
        try {
            List<Item> result=market.ListItems();
            for(Item item:result){
                System.out.println(item.toString());
            }
        } catch (RemoteException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void registerOrLogIn(){
        if(!accountNameTextField.getText().equals("")){
            try {
                Account account=bankobj.getAccount(accountNameTextField.getText());
                if(account == null)
                    try {
                        account = bankobj.newAccount(accountNameTextField.getText());
                    } catch (RejectedException ex) {
                        Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
                        statusChanged("Couldn't find or create account");
                        return;
                    }
                
                owner=new OwnerImpl(accountNameTextField.getText(), account);
                String result=market.Register(owner);
                if(result.contains("Name already exist"))
                    statusChanged("Logged in");
                
            } catch (RemoteException ex) {
                Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void sell(){
        float price;
        try{
            price=Float.parseFloat(itemPriceTextField.getText());
        }catch(NumberFormatException e){
            price=8080;
        }
        if(itemNameTextField.getText().equals(""))
            return;
        
        Item item=new Item(itemNameTextField.getText(), price, owner);
        try {
            String result=market.SellItem(item);
            statusChanged(result);
        } catch (RemoteException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
            statusChanged("Remote exception");
        }
    }
    
    public void connected(){
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                registerButton.setEnabled(true);
                accountNameTextField.setEnabled(true);
                listItemsButton.setEnabled(true);
                statusMessageLabel.setText("");
            }
        });
    }
    
    public void statusChanged(String statusMessage){
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("Client: Status = "+statusMessage);
                statusMessageLabel.setText(statusMessage);
            }
        });
    }
    
    public void messageReceived(String response){
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("Client: Response = "+response);
                currentGuessLabel.setText(response);
                if(response.contains("GAME OVER! Score ") || response.contains("Congratulations! Word"))
                    registerButton.setEnabled(false);
            }
        });
    }
}
