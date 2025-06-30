import { tokenUtils } from '../src/utils/tokenUtils'

export default defineNuxtRouteMiddleware((to) => {
    // Solo ejecutar en el cliente para evitar mismatch de hidratación
    if (!process.client) {
        return
    }
    
    const accessToken = tokenUtils.getToken()
    
    // Páginas que no requieren autenticación
    const publicPages = ['/landing', '/login', '/register']
    const isPublicPage = publicPages.includes(to.path)
    
    console.log('Auth middleware (client):', { 
        path: to.path, 
        hasToken: !!accessToken, 
        isPublic: isPublicPage,
        tokenValue: tokenUtils.getTokenPreview()
    })
    
    // Si no hay token y está intentando acceder a una página protegida
    if (!accessToken && !isPublicPage) {
        console.log('Redirecting to landing - no token for protected page')
        return navigateTo('/landing', { replace: true })
    }
    
    // Si tiene token y está intentando acceder a páginas públicas, redirigir a home
    if (accessToken && isPublicPage) {
        console.log('Redirecting to home - has token accessing public page')
        return navigateTo('/', { replace: true })
    }
    
    // Si tiene token, permitir acceso a cualquier página protegida
    if (accessToken && !isPublicPage) {
        console.log('Access granted - valid token for protected page')
        return
    }
    
    console.log('Auth middleware completed for:', to.path)
})