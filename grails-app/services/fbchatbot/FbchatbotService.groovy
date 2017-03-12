package fbchatbot

import grails.converters.JSON
import grails.converters.*
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseDecorator
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import grails.util.Environment

class FbchatbotService {

  private static final String TOKEN_FACEBOOK_PAGE = "TOKEN_FACEBOOK_PAGE"
  private static final String ID_PAGE_FACEBOOK    = "ID_PAGE"

  private def error(msg){
  	println "ERROR "+msg
  }

  private def info(msg){
  	println "INFO "+msg
  }

  def test(){

  	println "FbchatbotService      TOKEN_FACEBOOK_PAGE:"+TOKEN_FACEBOOK_PAGE
  	println "FbchatbotService ID_PAGE_FACEBOOK:"+ID_PAGE_FACEBOOK

  }

  def enviarMensagem(senderId,message){

    def retorno = message
    if (Environment.current == Environment.DEVELOPMENT){
      return retorno
    }

    def http = new HTTPBuilder("https://graph.facebook.com")
    def path = "/v2.6/me/messages"
    def query = [   access_token: TOKEN_FACEBOOK_PAGE ]

    def ret

    def jsonParams = [
                          recipient: [id:senderId],
                          message: [text:message ]
                        ]

    http.request(Method.POST, ContentType.JSON){

        uri.path  = path
        uri.query = query

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        body = jsonParams

        //headers.json = jsonParams

        // response handler for a success response code
        response.success = { resp, json ->

            info 'respEnvio: '+json

        }

        response.failure = { resp , json ->
            error("ChatbootmessengerService->enviarMensagem Falha - Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            error("ChatbootmessengerService->enviarMensagem Falha - body:"+json)
        }

    }

    return retorno
  }

  def enviarConfirmacao(senderId, titulo, textoResumo, payload,imageUrl=null){

    def retorno = titulo+" - "+textoResumo+"["+payload+"]"
    if (Environment.current == Environment.DEVELOPMENT){
      return retorno
    }

    def http = new HTTPBuilder("https://graph.facebook.com")
    def path = "/v2.6/me/messages"
    def query = [   access_token:TOKEN_FACEBOOK_PAGE ]

    def ret

    if(!imageUrl){
      imageUrl = """https://media.licdn.com/mpr/mpr/shrink_200_200/AAEAAQAAAAAAAAVPAAAAJDVlZDA4NjE2LWM1MDQtNDZmOC1iZjIyLWIxN2MyNWU3N2FhMg.png"""
    }

    def jsonParams = [
                          recipient: [id:senderId],
                          message: [ 
                                      attachment:[
                                            type:"template",
                                            payload:[
                                                  template_type:"generic",
                                                  elements:[
                                                    [
                                                      title:titulo,
                                                      image_url:imageUrl,
                                                      subtitle: textoResumo,
                                                      buttons:[
                                                        [
                                                          type:"postback",
                                                          title:"Confirmar",
                                                          payload:payload
                                                        ]
                                                      ]
                                                    ]
                                                    
                                                  ]
                                                ]

                                          ]
                                     ]
                        ]

    http.request(Method.POST, ContentType.JSON){

        uri.path  = path
        uri.query = query

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        body = jsonParams

        // response handler for a success response code
        response.success = { resp, json ->
            info 'respEnvio: '+json
        }

        response.failure = { resp , json ->
            error("ChatbootmessengerService->enviarConfirmacao Falha - Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            error("ChatbootmessengerService->enviarConfirmacao Falha - body:"+json)
        }

    }

    return retorno

  }

  def enviarImagemComOpcoes(senderId,titulo,textoResumo,imageUrl,opcoes){

    def retorno = "IMAGEM("+imageUrl+") "+titulo+" - "+textoResumo+"["+opcoes+"]"
    if (Environment.current == Environment.DEVELOPMENT){
      return retorno
    }

    def http = new HTTPBuilder("https://graph.facebook.com")
    def path = "/v2.6/me/messages"
    def query = [   access_token:TOKEN_FACEBOOK_PAGE ]

    def ret

    def jsonParams = [
                          recipient: [id:senderId],
                          message: [ 
                                      attachment:[
                                            type:"template",
                                            payload:[
                                                  template_type:"generic",
                                                  elements:[
                                                    [
                                                      title:titulo,
                                                      image_url:imageUrl,
                                                      subtitle: textoResumo,
                                                      buttons: opcoes 
                                                    ]
                                                    
                                                  ]
                                                ]

                                          ]
                                     ]
                        ]

    http.request(Method.POST, ContentType.JSON){

        uri.path  = path
        uri.query = query

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        body = jsonParams

        // response handler for a success response code
        response.success = { resp, json ->
            info 'respEnvio: '+json
        }

       response.failure = { resp , json ->
            error("ChatbootmessengerService->enviarImagemComOpcoes Falha - Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            error("ChatbootmessengerService->enviarImagemComOpcoes Falha - body:"+json)
        }

    }

    return retorno

  }

  def gerarUrlLocalMapaEstatico(lat,lng,strExtra){

    def linkMapaEstatico

    //withFeature('featureMapaYandex'){
      linkMapaEstatico = "https://static-maps.yandex.ru/1.x/?lang=en-US&ll="+lng+","+lat+"&z=18&l=map&size=200,100&pt="+lng+","+lat+",flag"
    //}
    //withoutFeature('featureMapaYandex'){
    //  linkMapaEstatico = "https://maps.googleapis.com/maps/api/staticmap?"+strExtra+"&maptype=roadmap\\&markers=size:mid%7Ccolor:0xF5C300FF%7C"+lat+","+lng+"&key="+GOOGLE_BROWSER_KEY
    //}

    return linkMapaEstatico

  }

  def enviarRecibo(senderId, entrega){

    def nomeNoRecibo      = entrega?.cliente?.nome
    def orderNumber       = String.valueOf(entrega.id)
    def detalhesPagamento 

    def valorPontoA = 0
    def valorPontoB = 0

    if(entrega.cobranca==0){
      detalhesPagamento = "Dinheiro na coleta"
      valorPontoA = entrega?.valor
      valorPontoB = 0
    }else{
      detalhesPagamento = "Dinheiro na entrega"
      valorPontoA = 0
      valorPontoB = entrega?.valor
    }

    def enderecoA   = entrega?.enderecos[0]?.rua+", "+entrega?.enderecos[0]?.numero
    def enderecoB   = entrega?.enderecos[1]?.rua+", "+entrega?.enderecos[1]?.numero
    def descCidade  = entrega?.cidade?.nome
    def uf          = entrega?.cidade?.estado?.nome

    def urlMapaEnderecoA = gerarUrlLocalMapaEstatico(entrega?.enderecos[0]?.latitude,entrega?.enderecos[0]?.longitude,"size=100x100")
    def urlMapaEnderecoB = gerarUrlLocalMapaEstatico(entrega?.enderecos[0]?.latitude,entrega?.enderecos[0]?.longitude,"size=100x100")

    def valorTotal       = entrega?.valor
    def cep              = entrega?.enderecos[0]?.cep

    def retorno = """RECIBO_ENTREGA | 
                               | nome: ${nomeNoRecibo}      
                               | id: ${orderNumber}       
                               | pagamento: ${detalhesPagamento} 
                               | enderecoA: ${enderecoA}   
                               | enderecoB: ${enderecoB}   
                               | descCidade: ${descCidade}  
                               | uf: ${uf}          
                               | urlMapaEnderecoA: ${urlMapaEnderecoA} 
                               | urlMapaEnderecoB: ${urlMapaEnderecoB} 
                               | valor: ${valorTotal}       
                               | cep: ${cep}"""

    if (Environment.current == Environment.DEVELOPMENT){
      return retorno
    }

    def http = new HTTPBuilder("https://graph.facebook.com")
    def path = "/v2.6/me/messages"
    def query = [   access_token:TOKEN_FACEBOOK_PAGE ]

    def ret

    def jsonParams = [
                          recipient: [id:senderId],
                          message: [ 
                                      "attachment":[
                                          "type":"template",
                                          "payload":[
                                            "template_type":"receipt",
                                            "recipient_name":nomeNoRecibo,
                                            "order_number":orderNumber,
                                            "currency":"BRL",
                                            "payment_method":detalhesPagamento,
                                            //"order_url":"http://petersapparel.parseapp.com/order?order_id=123456",
                                            "elements":[
                                              [
                                                "title":"Coleta em: "+enderecoA,
                                                "subtitle":"...",
                                                "image_url":urlMapaEnderecoA,
                                                //"quantity":2,
                                                "price":valorPontoA,
                                                "currency":"BRL"
                                              ],
                                              [
                                                "title":"Entrega em: "+enderecoB,
                                                "subtitle":"...",
                                                "image_url":urlMapaEnderecoB,
                                                //"quantity":2,
                                                "price":valorPontoB,
                                                "currency":"BRL"
                                              ]
                                            ],
                                            "address":[
                                              "street_1":enderecoB,
                                              "street_2":"",
                                              "city":descCidade,
                                              "postal_code":cep,
                                              "state":uf,
                                              "country":"BR"
                                            ],
                                            "summary":[
                                              //"subtotal":75.00,
                                              //"shipping_cost":4.95,
                                              //"total_tax":6.19,
                                              "total_cost":valorTotal
                                            ],
                                            /*
                                            "adjustments":[
                                              [
                                                "name":"New Customer Discount",
                                                "amount":20
                                              ],
                                              [
                                                "name":"$10 Off Coupon",
                                                "amount":10
                                              ]
                                            ]
                                            */
                                          ]
                                        ]
                                    ]
                              ]

    http.request(Method.POST, ContentType.JSON){

        uri.path  = path
        uri.query = query

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        body = jsonParams

        // response handler for a success response code
        response.success = { resp, json ->
            info 'respEnvio: '+json
        }

        response.failure = { resp , json ->
            error("ChatbootmessengerService->enviarRecibo Falha - Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            error("ChatbootmessengerService->enviarRecibo Falha - body:"+json)
        }

    }

    return retorno
  }

  def enviarListaOpcoes(senderId, titulo, opcoes){

    def retorno = titulo+" | opcoes: "+opcoes
    if (Environment.current == Environment.DEVELOPMENT){
      return retorno
    }

    def http = new HTTPBuilder("https://graph.facebook.com")
    def path = "/v2.6/me/messages"
    def query = [   access_token:TOKEN_FACEBOOK_PAGE ]

    def ret

    def jsonParams = [
                          recipient: [id:senderId],
                          message: [ 
                                      attachment:[
                                            type:"template",
                                            payload:[
                                              template_type:"button",
                                              text:titulo,
                                              buttons:opcoes
                                            ]
                                          ]
                                     ]
                        ]

    http.request(Method.POST, ContentType.JSON){

        uri.path  = path
        uri.query = query

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        body = jsonParams

        // response handler for a success response code
        response.success = { resp, json ->
            info 'respEnvio: '+json
        }

        response.failure = { resp , json ->
            error("ChatbootmessengerService->enviarListaOpcoes Falha - Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            error("ChatbootmessengerService->enviarListaOpcoes Falha - body:"+json)
        }


    }
    return retorno

  }

  def configurar(){
    ativarAppNovamente()
    //configurarMensagemInicio()
  }

  def ativarAppNovamente(){

    /*
     * Ativa novamente o APP caso o facebook tenha desativado.
    */

    def http = new HTTPBuilder("https://graph.facebook.com")
    def path = "/v2.6/me/subscribed_apps"
    def query = [   access_token:TOKEN_FACEBOOK_PAGE ]

    def ret

    http.request(Method.POST, ContentType.JSON){

        uri.path  = path
        uri.query = query

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        // response handler for a success response code
        response.success = { resp, json ->
            info 'respEnvio: '+json
        }

        response.failure = { resp , json ->
            error("ChatbootmessengerService->ativarAppNovamente Falha - Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            error("ChatbootmessengerService->ativarAppNovamente Falha - body:"+json)
        }

    }

  }

  def configurarMensagemInicio(){

    def http = new HTTPBuilder("https://graph.facebook.com")
    def path = "/v2.6/"+ID_PAGE_FACEBOOK+"/thread_settings"
    def query = [   access_token:TOKEN_FACEBOOK_PAGE ]

    def ret

    def jsonParams = [
                          setting_type:"call_to_actions",
                          thread_state:"new_thread",
                          call_to_actions:[
                            [
                              message:[
                                attachment:[
                                  type:"template",
                                  payload:[
                                    template_type:"generic",
                                    elements:[
                                      [
                                        title:"TITULO",
                                        item_url:"https://www.site.com",
                                        image_url:"https://media.licdn.com/mpr/mpr/shrink_200_200/AAEAAQAAAAAAAAVPAAAAJDVlZDA4NjE2LWM1MDQtNDZmOC1iZjIyLWIxN2MyNWU3N2FhMg.png",
                                        subtitle:"SUBITITULO",
                                        buttons:[

                                          [
                                            "type":"postback",
                                            "title":"Opção 2",
                                            "payload":"OP_OPCAO_2"
                                          ],

                                          [
                                            type:"postback",
                                            title:"chamar",
                                            payload:"OP_CHAMAR"
                                          ]
                                        ]
                                      ]
                                    ]
                                  ]
                                ]
                              ]
                            ]
                          ]
                        ]

    http.request(Method.POST, ContentType.JSON){

        uri.path  = path
        uri.query = query

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        body = jsonParams

        // response handler for a success response code
        response.success = { resp, json ->
            info 'respEnvio: '+json
        }

        response.failure = { resp , json ->
            error("ChatbootmessengerService->configurarMensagemInicio Falha - Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            error("ChatbootmessengerService->configurarMensagemInicio Falha - body:"+json)
        }

    }

  }

  def carregarInformacoesPerfil(userId){

    def http = new HTTPBuilder("https://graph.facebook.com")
    def path = "/v2.6/"+userId
    def query = [ fields:"first_name,last_name,profile_pic,locale,timezone,gender",
                  access_token:TOKEN_FACEBOOK_PAGE ]

    def ret = [nome:"",sobrenome:"",imagem:"",locale:"",timezone:"",gender:""]

    http.request(Method.GET, ContentType.JSON){

        uri.path  = path
        uri.query = query

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

        response.success = { resp, json ->

            info 'respEnvio: '+json

            ret.nome      = json.first_name
            ret.sobrenome = json.last_name
            ret.imagem    = json.profile_pic

            ret.locale    = json.locale
            ret.timezone  = json.timezone
            ret.gender    = json.gender

        }

        response.failure = { resp , json ->
            error("ChatbootmessengerService->carregarInformacoesPerfil Falha - Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            error("ChatbootmessengerService->carregarInformacoesPerfil Falha - body:"+json)
        }

    }

    return ret
  }

}