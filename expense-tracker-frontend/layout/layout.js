import Header from "./header"
import Footer from "./footer"   

export default function Layout(props){
    return(
        <div className="flex flex-col min-h-screen">
            <Header/>
            <div className="flex flex-col min-h-screen">
                <main className="mt-16">
                    {props.children}
                </main>
            </div>
            <Footer/>
        </div>
    )
}