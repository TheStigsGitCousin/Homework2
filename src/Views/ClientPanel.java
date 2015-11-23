/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package Views;

import homework2.Item;
import homework2.MarketRequest;
import homework2.Message;
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
import javax.swing.SwingConstants;
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
    private static List<Item> currentlyListedItems;
    
    // Game components
    private JLabel statusMessageLabel=new JLabel("STATUS MESSAGE:");
    private JLabel itemsLabel=new JLabel("Items", SwingConstants.CENTER);
    private JTextField accountNameTextField=new JTextField(10);
    private JButton registerButton=new JButton("Register/Log in");
    private JButton listItemsButton=new JButton("List items");
    // Connection components
    private JTextField itemNameTextField=new JTextField(10);
    private JTextField itemPriceTextField=new JTextField(10);
    private JButton sellButton=new JButton("sell");
    private JButton wishButton=new JButton("wish");
    
    private JTextField buyItemNameTextField=new JTextField(10);
    private JButton buyButton=new JButton("buy");
    
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
        
        buyButton.addActionListener((ActionEvent e)->{ buy(); });
        buyItemNameTextField.addActionListener((ActionEvent e)->{ buy(); });
        
        itemNameTextField.addActionListener((ActionEvent e)->{ sell(); });
        sellButton.addActionListener((ActionEvent e)->{ sell(); });
        wishButton.addActionListener((ActionEvent e)->{ wish(); });
        
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
        add(gamePanel, BorderLayout.NORTH);
        
        JPanel tPanel=new JPanel();
        tPanel.setLayout(new BorderLayout());
        
        JPanel sellAndWishPanel=new JPanel();
        sellAndWishPanel.setLayout(new FlowLayout());
        sellAndWishPanel.add(new JLabel("name"));
        sellAndWishPanel.add(itemNameTextField);
        sellAndWishPanel.add(new JLabel("price"));
        sellAndWishPanel.add(itemPriceTextField);
        sellAndWishPanel.add(sellButton);
        sellAndWishPanel.add(wishButton);
        // Add connectionPanel to ClientPanel
        tPanel.add(sellAndWishPanel, BorderLayout.NORTH);
        
        JPanel buyPanel=new JPanel();
        buyPanel.setLayout(new FlowLayout());
        buyPanel.add(new JLabel("id"));
        buyPanel.add(buyItemNameTextField);
        buyPanel.add(buyButton);
        // Add connectionPanel to ClientPanel
        tPanel.add(buyPanel, BorderLayout.CENTER);
        add(tPanel,BorderLayout.CENTER);
        
        JPanel itemsAndStatusPanel=new JPanel();
        itemsAndStatusPanel.setLayout(new BorderLayout());
        itemsAndStatusPanel.add(itemsLabel, BorderLayout.NORTH);
        itemsAndStatusPanel.add(statusMessageLabel, BorderLayout.CENTER);
        // Add connectionPanel to ClientPanel
        add(itemsAndStatusPanel, BorderLayout.SOUTH);
    }
    
    private void listItems(){
        try {
            currentlyListedItems=(List<Item>)market.ListItems().obj;
            System.out.println(currentlyListedItems.size());
            StringBuilder sb=new StringBuilder();
            int index=0;
            sb.append("format: [id]. [item]<br>");
            for(Item item:currentlyListedItems){
                System.out.println(item.toString());
                sb.append(index++).append(". ");
                sb.append(item.toString());
                sb.append("<br>");
            }
            itemsLabel.setText("<html>"+sb.toString()+"</html>");
            itemsLabel.getParent().revalidate();
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
                
                try {
                    account.deposit(100000);
                } catch (RejectedException ex) {
                    Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                owner=new OwnerImpl(accountNameTextField.getText(), account);
                Message msg=market.Register(owner);
                String result=msg.message;
                if(result.contains("Name already exist"))
                    statusChanged("Log in successful");
                else
                    statusChanged("Account registered and logged in");
                
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
            return;
        }
        if(itemNameTextField.getText().equals(""))
            return;
        
        Item item=new Item(itemNameTextField.getText(), price, owner);
        try {
            String result=market.SellItem(item).message;
            statusChanged(result);
        } catch (RemoteException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
            statusChanged("Remote exception");
        }
    }
    
    private void buy(){
        if(buyItemNameTextField.getText().equals(""))
            return;
        
        int index;
        try{
            index=Integer.parseInt(buyItemNameTextField.getText());
        }catch(NumberFormatException e){
            return;
        }
        
        if(index<0 || index>=currentlyListedItems.size())
            return;
        
        try {
            String result=market.BuyItem(currentlyListedItems.get(index).getId(), owner).message;
            statusChanged(result);
        } catch (RemoteException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
            statusChanged("Remote exception");
        }
    }
    
    private void wish(){
        float price;
        try{
            price=Float.parseFloat(itemPriceTextField.getText());
        }catch(NumberFormatException e){
            return;
        }
        if(itemNameTextField.getText().equals(""))
            return;
        
        Item item=new Item(itemNameTextField.getText(), price, owner);
        try {
            String result=market.AddWish(item).message;
            statusChanged(result);
        } catch (RemoteException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
            statusChanged("Remote exception");
        }
    }
    
    private void logIn(){
        try {
            Message msg=market.GetUser(accountNameTextField.getText());
            owner=(Owner)msg.obj;
        } catch (RemoteException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
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
                itemsLabel.setText(response);
                if(response.contains("GAME OVER! Score ") || response.contains("Congratulations! Word"))
                    registerButton.setEnabled(false);
            }
        });
    }
}
