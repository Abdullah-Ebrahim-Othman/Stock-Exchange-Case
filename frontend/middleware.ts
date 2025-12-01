import {NextResponse} from 'next/server';
import type {NextRequest} from 'next/server';

export function middleware(request: NextRequest) {
	const token = request.cookies.get('jwt')?.value;

	const isPublicApi = request.nextUrl.pathname.startsWith('/api/auth/');
	if (isPublicApi) {
		return NextResponse.next();
	}

	const isAuthPage = request.nextUrl.pathname.startsWith('/login') ||
		request.nextUrl.pathname.startsWith('/register');

	if (!token && !isAuthPage) {
		const loginUrl = new URL('/login', request.url);
		loginUrl.searchParams.set('from', request.nextUrl.pathname);
		return NextResponse.redirect(loginUrl);
	}

	if (token && request.nextUrl.pathname.startsWith('/login')) {
		return NextResponse.redirect(new URL('/dashboard', request.url));
	}

	if (request.nextUrl.pathname.startsWith('/api/')) {


		const response = NextResponse.next({
			request: {
				headers: new Headers(request.headers),
			},
		});

		return response;
	}

	return NextResponse.next();
}

export const config = {
	matcher: [
		'/((?!_next/static|_next/image|favicon.ico).*)',
		'/dashboard/:path*',
		'/profile/:path*',
		'/api/:path*'
	],
};