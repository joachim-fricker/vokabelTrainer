from lib import make
groups = [
("Wetter", """
tempo|o tempo|das Wetter; die Zeit|n
sol|o sol|die Sonne|n
chuva|a chuva|der Regen|n
chover|chover|regnen|v|Está chovendo. = Es regnet.
vento|o vento|der Wind|n
nuvem|a nuvem|die Wolke|n
nublado|nublado|bewölkt|adj
calor|o calor|die Hitze|n|Faz calor. = Es ist heiss.
temperatura|a temperatura|die Temperatur|n
grau|o grau|das Grad|n|trinta graus = 30 Grad
tempestade|a tempestade|das Gewitter, der Sturm|n
umido|úmido|feucht, schwül|adj
"""),
("Am Strand", """
praia|a praia|der Strand|n
mar|o mar|das Meer|n
areia|a areia|der Sand|n
onda|a onda|die Welle|n
ilha|a ilha|die Insel|n
guarda_sol|o guarda-sol|der Sonnenschirm|n
cadeira_praia|a cadeira de praia|der Strandstuhl|n
canga|a canga|das Strandtuch (Sarong)|n|Typisch brasilianisch statt Badetuch.
salva_vidas|o salva-vidas|der Rettungsschwimmer|n
nadar|nadar|schwimmen|v
mergulhar|mergulhar|tauchen|v
surfar|surfar|surfen|v
"""),
("Natur & Ausflüge", """
passeio|o passeio|der Ausflug, der Spaziergang|n
montanha|a montanha|der Berg|n
rio|o rio|der Fluss|n
lago|o lago|der See|n
cachoeira|a cachoeira|der Wasserfall|n
floresta|a floresta|der Wald|n|a Floresta Amazônica = der Amazonas-Regenwald
trilha|a trilha|der Wanderweg|n|fazer trilha = wandern
parque|o parque|der Park|n
por_do_sol|o pôr do sol|der Sonnenuntergang|n
"""),
("Ausgehen & Kultur", """
show|o show|das Konzert, die Show|n
ingresso|o ingresso|die Eintrittskarte|n
cinema|o cinema|das Kino|n
teatro|o teatro|das Theater|n
festa|a festa|das Fest, die Party|n
carnaval|o carnaval|der Karneval|n
samba|o samba|der Samba|n
barzinho|o barzinho|die (kleine) Bar|n|Treffpunkt nach der Arbeit.
jogo|o jogo|das Spiel|n
estadio|o estádio|das Stadion|n
"""),
("Verben", """
fazer|fazer|machen, tun|v|Unregelmässig: eu faço, você faz
descansar|descansar|sich ausruhen|v
aproveitar|aproveitar|geniessen, nutzen|v|Aproveite! = Viel Spass! / Geniess es!
dancar|dançar|tanzen|v
jogar|jogar|spielen (Spiel, Sport)|v
assistir|assistir|anschauen, zuschauen|v|assistir a um jogo / um filme
abrir|abrir|öffnen|v
fechar|fechar|schliessen|v
"""),
]
sentences = [
("Como está o tempo hoje?","Wie ist das Wetter heute?","como tempo hoje","ser_estar"),
("Hoje faz sol e muito calor.","Heute scheint die Sonne und es ist sehr heiss.","hoje sol calor muito","wetter"),
("Está chovendo desde hoje de manhã.","Es regnet seit heute Morgen.","chover hoje","wetter"),
("Vai chover amanhã?","Wird es morgen regnen?","chover amanha ir","ir_futuro wetter"),
("Leve um guarda-chuva, vai ter chuva.","Nimm einen Regenschirm mit, es wird Regen geben.","chuva levar","ir_futuro"),
("Hoje está nublado e úmido.","Heute ist es bewölkt und schwül.","hoje nublado umido","wetter"),
("Tem uma nuvem preta no céu.","Am Himmel ist eine schwarze Wolke.","nuvem preto","ter_haben"),
("Está ventando muito na praia.","Am Strand ist es sehr windig.","muito praia","wetter"),
("O vento está forte hoje.","Der Wind ist heute stark.","vento hoje"),
("Qual é a temperatura agora?","Wie viel Grad hat es jetzt?","qual temperatura"),
("Está fazendo trinta e cinco graus!","Es hat fünfunddreissig Grad!","num_30 num_5 grau","wetter"),
("No inverno faz frio no sul do Brasil.","Im Winter ist es im Süden Brasiliens kalt.","frio brasil","wetter praep_artikel"),
("Cuidado, vem uma tempestade.","Vorsicht, ein Gewitter kommt.","tempestade vir"),
("Vamos à praia hoje?","Gehen wir heute an den Strand?","praia hoje ir","ir_futuro praep_artikel"),
("A água do mar está quente.","Das Meerwasser ist warm.","agua mar quente","praep_artikel"),
("A areia está muito quente.","Der Sand ist sehr heiss.","areia muito quente"),
("As ondas estão grandes hoje.","Die Wellen sind heute hoch.","onda grande hoje"),
("É perigoso nadar aqui?","Ist es gefährlich, hier zu schwimmen?","perigoso nadar aqui"),
("Eu nado todo dia de manhã.","Ich schwimme jeden Morgen.","eu nadar","praesens"),
("Nade perto do salva-vidas.","Schwimm in der Nähe des Rettungsschwimmers.","nadar perto salva_vidas","anweisungen"),
("Quanto custa alugar um guarda-sol e duas cadeiras de praia?","Was kostet es, einen Sonnenschirm und zwei Strandstühle zu mieten?","alugar guarda_sol cadeira_praia num_2"),
("Esqueci a minha canga no hotel.","Ich habe mein Strandtuch im Hotel vergessen.","canga hotel","praep_artikel"),
("Você sabe surfar?","Kannst du surfen?","voce surfar"),
("Eles surfam muito bem.","Sie surfen sehr gut.","eles muito bem","praesens"),
("Vamos mergulhar perto da ilha.","Wir gehen in der Nähe der Insel tauchen.","mergulhar perto ilha ir","ir_futuro"),
("O passeio de barco sai às nove.","Der Bootsausflug fährt um neun ab.","passeio barco num_9 sair"),
("Vamos fazer uma trilha na montanha.","Wir machen eine Wanderung in den Bergen.","fazer trilha montanha ir","ir_futuro"),
("A trilha até a cachoeira leva duas horas.","Der Weg zum Wasserfall dauert zwei Stunden.","trilha cachoeira levar num_2 hora"),
("O rio é muito bonito e limpo.","Der Fluss ist sehr schön und sauber.","rio muito bonito limpo"),
("Dá para nadar no lago?","Kann man im See schwimmen?","nadar lago","praep_artikel"),
("A floresta é cheia de animais.","Der Wald ist voller Tiere.","floresta"),
("A gente vai ver o pôr do sol na praia.","Wir schauen uns den Sonnenuntergang am Strand an.","a_gente por_do_sol praia ir","a_gente ir_futuro"),
("O parque fecha às seis.","Der Park schliesst um sechs.","parque fechar num_6","praesens"),
("O museu abre às dez.","Das Museum öffnet um zehn.","museu abrir num_10","praesens"),
("Tem show de samba hoje à noite.","Heute Abend gibt es ein Samba-Konzert.","show samba hoje noite","ter_haben"),
("Onde eu compro ingressos?","Wo kaufe ich Eintrittskarten?","onde eu comprar ingresso","praesens"),
("Dois ingressos para o show, por favor.","Zwei Karten für das Konzert, bitte.","num_2 ingresso show por_favor"),
("Vamos ao cinema ou ao teatro?","Gehen wir ins Kino oder ins Theater?","cinema teatro ir","ir_futuro praep_artikel"),
("Sábado tem uma festa na casa dele.","Am Samstag ist eine Party bei ihm.","festa","ter_haben"),
("O carnaval é em fevereiro.","Der Karneval ist im Februar.","carnaval","ser_estar"),
("Você dança samba?","Tanzt du Samba?","voce dancar samba","praesens"),
("Eu danço muito mal!","Ich tanze sehr schlecht!","eu dancar muito","praesens"),
("Vamos tomar uma cerveja num barzinho?","Trinken wir ein Bier in einer Bar?","tomar cerveja barzinho ir","ir_futuro"),
("Você quer assistir ao jogo no estádio?","Willst du dir das Spiel im Stadion anschauen?","voce assistir jogo estadio","praep_artikel"),
("Eu jogo futebol com os amigos.","Ich spiele mit Freunden Fussball.","eu jogar futebol com","praesens"),
("Hoje eu só quero descansar.","Heute will ich mich nur ausruhen.","hoje eu descansar"),
("Aproveite as férias!","Geniess den Urlaub!","aproveitar ferias","anweisungen"),
("O que você vai fazer no fim de semana?","Was machst du am Wochenende?","o_que voce fazer ir","ir_futuro"),
("Eu leio um livro na praia.","Ich lese am Strand ein Buch.","eu ler praia","praesens"),
("Vocês moram perto do mar?","Wohnt ihr in der Nähe des Meeres?","morar perto mar","praesens praep_artikel"),
("Nós aproveitamos muito o sol.","Wir geniessen die Sonne sehr.","nos aproveitar muito sol","praesens"),
("O tempo passa rápido nas férias.","Im Urlaub vergeht die Zeit schnell.","tempo ferias"),
]
grammar = [
 {"id":"praesens","title":"Regelmässige Verben im Präsens",
  "explanation":"Man nimmt die Endung -ar, -er oder -ir weg und hängt an:\n\nfalar (sprechen):  eu falo, você/ele fala, nós falamos, vocês/eles falam\ncomer (essen):  eu como, você/ele come, nós comemos, vocês/eles comem\nabrir (öffnen):  eu abro, você/ele abre, nós abrimos, vocês/eles abrem\n\nDie eu-Form endet immer auf -o.\nDa você, ele, ela und a gente dieselbe Form haben, braucht man in Brasilien nur vier Formen.",
  "examples":[{"text":"Eu nado no mar.","de":"Ich schwimme im Meer."},{"text":"Você mora aqui?","de":"Wohnst du hier?"},{"text":"Nós comemos peixe.","de":"Wir essen Fisch."},{"text":"A loja abre às nove.","de":"Der Laden öffnet um neun."}]},
 {"id":"wetter","title":"Über das Wetter reden",
  "explanation":"• fazer + Nomen: Faz sol. Faz calor. Faz frio. (Es ist sonnig/heiss/kalt.)\n• estar + Adjektiv: Está nublado. Está quente.\n• Was gerade passiert: estar + Verlaufsform (-ando, -endo, -indo)\n  chover → Está chovendo. (Es regnet gerade.)\n  ventar → Está ventando. (Es ist windig.)\n  fazer → Está fazendo calor.\n\nDiese Verlaufsform benutzt man in Brasilien für alles, was gerade geschieht: Estou estudando. (Ich lerne gerade.)",
  "examples":[{"text":"Hoje faz muito calor.","de":"Heute ist es sehr heiss."},{"text":"Está chovendo muito.","de":"Es regnet stark."},{"text":"O que você está fazendo?","de":"Was machst du gerade?"}]},
]
wg = {"chover":["wetter"],"calor":["wetter"],"tempo":["wetter"],"fazer":["wetter"]}
make('09_wetter_strand_freizeit.json','pt-BR-09',9,'Wetter, Strand & Freizeit','Tempo, praia e lazer',groups,sentences,grammar,wg)
