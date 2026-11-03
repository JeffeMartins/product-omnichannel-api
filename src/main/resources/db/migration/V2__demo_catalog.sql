INSERT INTO products VALUES
('FONE-BT-001','fone-bluetooth-aurora-grafite','Fone Bluetooth Aurora','Seu ritmo, sem interrupções. Áudio envolvente, almofadas confortáveis e até 40 horas de bateria para acompanhar cada parte do seu dia.','Aurora','Áudio','AURORA-HEADPHONES','Grafite','#292e37','/pdp-assets/products/aurora-graphite.svg',true),
('FONE-BT-AZUL','fone-bluetooth-aurora-azul','Fone Bluetooth Aurora Azul','O mesmo som envolvente, em uma nova cor. Conexão estável e conforto para trabalhar, viajar ou desacelerar.','Aurora','Áudio','AURORA-HEADPHONES','Azul','#426476','/pdp-assets/products/aurora-blue.svg',true),
('FONE-BT-AREIA','fone-bluetooth-aurora-areia','Fone Bluetooth Aurora Areia','Design leve, acabamento suave e espaço para ouvir o que importa. Uma experiência sonora para o seu cotidiano.','Aurora','Áudio','AURORA-HEADPHONES','Areia','#d4c2a8','/pdp-assets/products/aurora-sand.svg',true);
INSERT INTO locations VALUES ('CD-DEMO-01','Entrega em casa','WEB'),('LOJA-CENTRO','Retirada · Centro','PICKUP'),('LOJA-PAULISTA','Retirada · Paulista','PICKUP');
INSERT INTO offers(sku,channel,location_id,list_price,price)
 SELECT p.sku,l.channel,l.id,249.90,
 CASE WHEN p.sku='FONE-BT-AZUL' THEN 219.90 WHEN l.id='LOJA-PAULISTA' THEN 189.90 ELSE 199.90 END
 FROM products p CROSS JOIN locations l;
INSERT INTO inventory(sku,location_id,quantity)
 SELECT p.sku,l.id,CASE WHEN p.sku='FONE-BT-AREIA' THEN 0 WHEN l.id='LOJA-CENTRO' THEN 3 ELSE 18 END
 FROM products p CROSS JOIN locations l;
